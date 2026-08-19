package com.support.backend.dto;

import com.support.backend.enums.TicketStatus;

public record StatusCount(TicketStatus status, long count) {
}
