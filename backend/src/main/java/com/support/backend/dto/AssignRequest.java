package com.support.backend.dto;

/**
 * Manual assignment. A null {@code adminId} unassigns the ticket
 * (removes the current assignee).
 */
public record AssignRequest(Long adminId) {
}
