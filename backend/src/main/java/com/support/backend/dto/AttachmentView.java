package com.support.backend.dto;

public record AttachmentView(Long id, String filename, String contentType, long sizeBytes) {
}
