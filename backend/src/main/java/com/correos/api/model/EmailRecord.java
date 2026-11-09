package com.correos.api.model;

import java.time.LocalDateTime;

public record EmailRecord(
        Long id,
        String to,
        String subject,
        String message,
        EmailStatus status,
        String detail,
        LocalDateTime sentAt
) {
}
