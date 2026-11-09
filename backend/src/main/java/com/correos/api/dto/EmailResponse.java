package com.correos.api.dto;

import com.correos.api.model.EmailRecord;

public record EmailResponse(boolean success, String message, EmailRecord email) {
}
