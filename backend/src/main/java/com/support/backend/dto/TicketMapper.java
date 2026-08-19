package com.support.backend.dto;

import com.support.backend.entity.Admin;
import com.support.backend.entity.Attachment;
import com.support.backend.entity.Category;
import com.support.backend.entity.Ticket;
import com.support.backend.entity.TicketMessage;
import java.util.List;

/** Plain static entity -> DTO mapping, mirroring the lightweight approach used elsewhere. */
public final class TicketMapper {

    private TicketMapper() {
    }

    public static CategoryView toCategoryView(Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryView(category.getId(), category.getName(), category.getColor());
    }

    public static AdminView toAdminView(Admin admin) {
        if (admin == null) {
            return null;
        }
        return new AdminView(admin.getId(), admin.getEmail());
    }

    public static AttachmentView toAttachmentView(Attachment attachment) {
        return new AttachmentView(
                attachment.getId(),
                attachment.getFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes());
    }

    public static MessageView toMessageView(TicketMessage message, List<Attachment> attachments) {
        return new MessageView(
                message.getId(),
                message.getDirection(),
                message.getAuthorType(),
                message.getAuthorAdmin() != null ? message.getAuthorAdmin().getEmail() : null,
                message.getBody(),
                message.getCreatedAt(),
                attachments.stream().map(TicketMapper::toAttachmentView).toList());
    }

    public static TicketSummary toSummary(Ticket ticket, long messageCount) {
        return new TicketSummary(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getCustomerEmail(),
                ticket.getCustomerName(),
                ticket.getStatus(),
                toCategoryView(ticket.getCategory()),
                toAdminView(ticket.getAssignedAdmin()),
                messageCount,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }

    public static TicketDetail toDetail(Ticket ticket, List<MessageView> messages) {
        return new TicketDetail(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getCustomerEmail(),
                ticket.getCustomerName(),
                ticket.getStatus(),
                toCategoryView(ticket.getCategory()),
                toAdminView(ticket.getAssignedAdmin()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                messages);
    }
}
