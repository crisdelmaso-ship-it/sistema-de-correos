package com.correos.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TemplateRequest(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El asunto es obligatorio") String subject,
        @NotBlank(message = "El contenido es obligatorio") String body
) {
}
