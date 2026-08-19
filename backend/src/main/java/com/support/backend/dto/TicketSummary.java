package com.support.backend.dto;

import com.support.backend.enums.TicketStatus;
import java.time.Instant;

/** One row in the admin ticket queue. */
public record TicketSummary(
        Long id,
        String subject,
        String customerEmail,
        String customerName,
        TicketStatus status,
        CategoryView category,
        AdminView assignee,
        long messageCount,
        Instant createdAt,
        Instant updatedAt) {
}
