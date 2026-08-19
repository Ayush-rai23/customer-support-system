package com.support.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ReplyRequest(@NotBlank String body) {
}
