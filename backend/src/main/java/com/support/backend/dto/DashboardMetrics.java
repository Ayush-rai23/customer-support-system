package com.support.backend.dto;

import java.util.List;

/**
 * {@code avgResponseTimeMinutes} is null when no ticket in range has a reply yet.
 * {@code ticketsByStatus} is always all-time (current backlog), independent of range.
 */
public record DashboardMetrics(
        int rangeDays,
        Double avgResponseTimeMinutes,
        long ticketsInRange,
        List<StatusCount> ticketsByStatus,
        List<DailyCategoryCount> ticketsByCategoryPerDay) {
}
