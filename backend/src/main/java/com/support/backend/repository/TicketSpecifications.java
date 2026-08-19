package com.support.backend.repository;

import com.support.backend.entity.Ticket;
import com.support.backend.enums.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

/**
 * Static {@link Specification} builders for the optional, combinable ticket-queue
 * filters (status / category / assignee). Composed with {@code Specification.allOf(...)}
 * in {@code TicketService}; an absent filter returns {@link Specification#unrestricted()}
 * (a match-all no-op) rather than {@code null}, since {@code allOf} rejects null specs.
 */
public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> hasStatus(TicketStatus status) {
        if (status == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> hasCategory(Long categoryId) {
        if (categoryId == null) {
            return Specification.unrestricted();
        }
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Filter by assignee. A value of {@code -1} means "unassigned" (no admin);
     * any other id matches that admin.
     */
    public static Specification<Ticket> hasAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return Specification.unrestricted();
        }
        if (assigneeId == -1L) {
            return (root, query, cb) -> cb.isNull(root.get("assignedAdmin"));
        }
        return (root, query, cb) -> cb.equal(root.get("assignedAdmin").get("id"), assigneeId);
    }
}
