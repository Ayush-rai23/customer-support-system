package com.support.backend.service;

import com.support.backend.dto.MessageView;
import com.support.backend.dto.TicketDetail;
import com.support.backend.dto.TicketMapper;
import com.support.backend.dto.TicketSummary;
import com.support.backend.entity.Admin;
import com.support.backend.entity.Attachment;
import com.support.backend.entity.Category;
import com.support.backend.entity.Ticket;
import com.support.backend.entity.TicketMessage;
import com.support.backend.enums.AuthorType;
import com.support.backend.enums.MessageDirection;
import com.support.backend.enums.TicketStatus;
import com.support.backend.exception.InvalidTransitionException;
import com.support.backend.exception.NotFoundException;
import com.support.backend.repository.AdminRepository;
import com.support.backend.repository.AttachmentRepository;
import com.support.backend.repository.CategoryRepository;
import com.support.backend.repository.TicketMessageRepository;
import com.support.backend.repository.TicketRepository;
import com.support.backend.repository.TicketSpecifications;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    /**
     * Allowed manual (admin-driven) status transitions. Setting a ticket to its current
     * status is always a no-op; anything not listed here is rejected with 409.
     */
    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(TicketStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TicketStatus.NEW,
                Set.of(TicketStatus.OPEN, TicketStatus.ESCALATED, TicketStatus.PENDING_CUSTOMER,
                        TicketStatus.RESOLVED, TicketStatus.SPAM));
        ALLOWED_TRANSITIONS.put(TicketStatus.OPEN,
                Set.of(TicketStatus.PENDING_CUSTOMER, TicketStatus.ESCALATED, TicketStatus.RESOLVED,
                        TicketStatus.CLOSED, TicketStatus.SPAM));
        ALLOWED_TRANSITIONS.put(TicketStatus.ESCALATED,
                Set.of(TicketStatus.OPEN, TicketStatus.PENDING_CUSTOMER, TicketStatus.RESOLVED,
                        TicketStatus.CLOSED, TicketStatus.SPAM));
        ALLOWED_TRANSITIONS.put(TicketStatus.PENDING_CUSTOMER,
                Set.of(TicketStatus.OPEN, TicketStatus.ESCALATED, TicketStatus.RESOLVED,
                        TicketStatus.CLOSED, TicketStatus.SPAM));
        ALLOWED_TRANSITIONS.put(TicketStatus.AUTO_RESOLVED,
                Set.of(TicketStatus.OPEN, TicketStatus.ESCALATED, TicketStatus.PENDING_CUSTOMER,
                        TicketStatus.RESOLVED, TicketStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(TicketStatus.RESOLVED,
                Set.of(TicketStatus.OPEN, TicketStatus.CLOSED));
        ALLOWED_TRANSITIONS.put(TicketStatus.CLOSED,
                Set.of(TicketStatus.OPEN));
        ALLOWED_TRANSITIONS.put(TicketStatus.SPAM,
                Set.of(TicketStatus.NEW, TicketStatus.OPEN));
    }

    /** Statuses a new admin reply pulls back into active work. */
    private static final Set<TicketStatus> REPLY_REOPENS =
            Set.of(TicketStatus.NEW, TicketStatus.ESCALATED, TicketStatus.PENDING_CUSTOMER);

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final AttachmentRepository attachmentRepository;
    private final CategoryRepository categoryRepository;
    private final AdminRepository adminRepository;

    public TicketService(
            TicketRepository ticketRepository,
            TicketMessageRepository messageRepository,
            AttachmentRepository attachmentRepository,
            CategoryRepository categoryRepository,
            AdminRepository adminRepository) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.categoryRepository = categoryRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional(readOnly = true)
    public Page<TicketSummary> page(TicketStatus status, Long categoryId, Long assigneeId, Pageable pageable) {
        Specification<Ticket> spec = Specification.allOf(
                TicketSpecifications.hasStatus(status),
                TicketSpecifications.hasCategory(categoryId),
                TicketSpecifications.hasAssignee(assigneeId));
        return ticketRepository.findAll(spec, pageable)
                .map(ticket -> TicketMapper.toSummary(ticket, messageRepository.countByTicketId(ticket.getId())));
    }

    @Transactional(readOnly = true)
    public TicketDetail detail(Long ticketId) {
        Ticket ticket = getTicket(ticketId);
        List<TicketMessage> messages = messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        Map<Long, List<Attachment>> attachmentsByMessage = loadAttachments(messages);
        List<MessageView> views = messages.stream()
                .map(m -> TicketMapper.toMessageView(m,
                        attachmentsByMessage.getOrDefault(m.getId(), List.of())))
                .toList();
        return TicketMapper.toDetail(ticket, views);
    }

    @Transactional
    public TicketDetail addAdminReply(Long ticketId, String body, Admin author) {
        Ticket ticket = getTicket(ticketId);
        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .direction(MessageDirection.OUTBOUND)
                .authorType(AuthorType.ADMIN)
                .authorAdmin(author)
                .body(body)
                .build();
        messageRepository.save(message);

        if (REPLY_REOPENS.contains(ticket.getStatus())) {
            ticket.setStatus(TicketStatus.OPEN);
            ticketRepository.save(ticket);
        }
        return detail(ticketId);
    }

    @Transactional
    public TicketDetail changeStatus(Long ticketId, TicketStatus target) {
        Ticket ticket = getTicket(ticketId);
        TicketStatus current = ticket.getStatus();
        if (current != target) {
            if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
                throw new InvalidTransitionException(
                        "Cannot change status from " + current + " to " + target);
            }
            ticket.setStatus(target);
            ticketRepository.save(ticket);
        }
        return detail(ticketId);
    }

    @Transactional
    public TicketDetail overrideCategory(Long ticketId, Long categoryId) {
        Ticket ticket = getTicket(ticketId);
        if (categoryId == null) {
            ticket.setCategory(null);
        } else {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category " + categoryId + " not found"));
            ticket.setCategory(category);
        }
        ticketRepository.save(ticket);
        return detail(ticketId);
    }

    @Transactional
    public TicketDetail assign(Long ticketId, Long adminId) {
        Ticket ticket = getTicket(ticketId);
        if (adminId == null) {
            ticket.setAssignedAdmin(null);
        } else {
            Admin admin = adminRepository.findById(adminId)
                    .orElseThrow(() -> new NotFoundException("Admin " + adminId + " not found"));
            ticket.setAssignedAdmin(admin);
        }
        ticketRepository.save(ticket);
        return detail(ticketId);
    }

    private Ticket getTicket(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket " + ticketId + " not found"));
    }

    private Map<Long, List<Attachment>> loadAttachments(List<TicketMessage> messages) {
        if (messages.isEmpty()) {
            return Map.of();
        }
        List<Long> messageIds = messages.stream().map(TicketMessage::getId).toList();
        return attachmentRepository.findByMessageIdIn(messageIds).stream()
                .collect(Collectors.groupingBy(a -> a.getMessage().getId(),
                        Collectors.mapping(Function.identity(), Collectors.toList())));
    }
}
