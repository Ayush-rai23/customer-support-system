package com.support.backend.enums;

/**
 * Ticket lifecycle states, matching the model in PROJECT_PLAN.md and the
 * {@code --color-status-*} design tokens in the frontend {@code index.css}.
 */
public enum TicketStatus {
    NEW,
    AUTO_RESOLVED,
    PENDING_CUSTOMER,
    OPEN,
    ESCALATED,
    RESOLVED,
    CLOSED,
    SPAM
}
