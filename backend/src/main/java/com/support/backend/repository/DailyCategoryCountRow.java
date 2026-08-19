package com.support.backend.repository;

import java.time.Instant;

/** Native-query projection row for {@link TicketRepository#dailyCategoryCounts}. */
public interface DailyCategoryCountRow {
    Instant getDay();

    String getCategoryName();

    Long getCnt();
}
