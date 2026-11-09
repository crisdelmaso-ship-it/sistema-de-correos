package com.correos.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailRequest(
        @NotBlank(message = "El destinatario es obligatorio")
        @Email(message = "El destinatario no es un correo válido")
        String to,

        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 150, message = "El asunto no puede superar 150 caracteres")
        String subject,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 10000, message = "El mensaje no puede superar 10000 caracteres")
        String message
) {
}
