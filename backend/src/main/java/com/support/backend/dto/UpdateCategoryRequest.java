package com.support.backend.dto;

/**
 * Manual category override. A null {@code categoryId} clears the category
 * (sets it back to uncategorized).
 */
public record UpdateCategoryRequest(Long categoryId) {
}
