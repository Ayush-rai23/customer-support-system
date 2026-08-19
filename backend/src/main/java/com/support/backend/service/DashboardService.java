package com.support.backend.service;

import com.support.backend.dto.DailyCategoryCount;
import com.support.backend.dto.DashboardMetrics;
import com.support.backend.repository.TicketRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetrics metrics(int days) {
        Instant since = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(days - 1L, ChronoUnit.DAYS);

        List<DailyCategoryCount> byCategory = ticketRepository.dailyCategoryCounts(since).stream()
                .map(row -> new DailyCategoryCount(
                        row.getDay().atZone(ZoneOffset.UTC).toLocalDate(),
                        row.getCategoryName(),
                        row.getCnt()))
                .toList();

        long ticketsInRange = byCategory.stream().mapToLong(DailyCategoryCount::count).sum();

        return new DashboardMetrics(
                days,
                ticketRepository.averageResponseMinutes(since),
                ticketsInRange,
                ticketRepository.countByStatus(),
                byCategory);
    }
}
