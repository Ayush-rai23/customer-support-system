package com.support.backend.dto;

import com.support.backend.enums.TicketStatus;
import java.time.Instant;
import java.util.List;

/** Full ticket view: summary fields plus the ordered message thread. */
public record TicketDetail(
        Long id,
        String subject,
        String customerEmail,
        String customerName,
        TicketStatus status,
        CategoryView category,
        AdminView assignee,
        Instant createdAt,
        Instant updatedAt,
        List<MessageView> messages) {
}
