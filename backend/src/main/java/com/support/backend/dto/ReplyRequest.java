package com.support.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReplyRequest(
        @NotBlank @Size(min = 20, max = 700, message = "Reply must be between 20 and 700 characters") String body) {
}
