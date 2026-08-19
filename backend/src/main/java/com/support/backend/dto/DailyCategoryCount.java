package com.support.backend.dto;

import java.time.LocalDate;

public record DailyCategoryCount(LocalDate date, String categoryName, long count) {
}
