package com.support.backend.dto;

import com.support.backend.enums.AuthorType;
import com.support.backend.enums.MessageDirection;
import java.time.Instant;
import java.util.List;

public record MessageView(
        Long id,
        MessageDirection direction,
        AuthorType authorType,
        String authorEmail,
        String body,
        Instant createdAt,
        List<AttachmentView> attachments) {
}
