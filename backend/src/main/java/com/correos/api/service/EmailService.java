package com.correos.api.service;

import com.correos.api.dto.EmailRequest;
import com.correos.api.model.EmailRecord;
import com.correos.api.model.EmailStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;
    private final String relayUrl;
    private final String relaySecret;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final List<EmailRecord> history = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong idGenerator = new AtomicLong(1);

    public EmailService(JavaMailSender mailSender,
                        ObjectMapper objectMapper,
                        @Value("${spring.mail.username:}") String fromAddress,
                        @Value("${app.mail.from-name:Sistema de Correos}") String fromName,
                        @Value("${app.mail.relay-url:}") String relayUrl,
                        @Value("${app.mail.relay-secret:}") String relaySecret) {
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.relayUrl = relayUrl;
        this.relaySecret = relaySecret;
    }

    public EmailRecord send(EmailRequest request) {
        EmailStatus status;
        String detail;
        if (!relayUrl.isBlank()) {
            try {
                sendViaRelay(request);
                status = EmailStatus.SENT;
                detail = "Correo enviado correctamente";
            } catch (IOException | RuntimeException ex) {
                log.error("Error enviando correo por el relay de Gmail", ex);
                status = EmailStatus.FAILED;
                detail = "No se pudo enviar el correo: "
                        + (ex.getMessage() != null ? ex.getMessage() : "fallo de conexión con el relay (" + ex.getClass().getSimpleName() + ")");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                status = EmailStatus.FAILED;
                detail = "No se pudo enviar el correo: envío interrumpido";
            }
            return saveRecord(request, status, detail);
        }
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress, fromName);
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(request.message(), toHtml(request.message()));
            mailSender.send(mime);
            status = EmailStatus.SENT;
            detail = "Correo enviado correctamente";
        } catch (MailException | MessagingException | UnsupportedEncodingException ex) {
            log.error("Error enviando correo por Gmail", ex);
            status = EmailStatus.FAILED;
            detail = "No se pudo enviar el correo: " + ex.getMessage();
        }
        return saveRecord(request, status, detail);
    }

    /** Envía el correo por HTTPS a un script de Google Apps Script que usa la cuenta de Gmail. */
    private void sendViaRelay(EmailRequest request) throws IOException, InterruptedException {
        String json = objectMapper.writeValueAsString(Map.of(
                "secret", relaySecret,
                "to", request.to(),
                "subject", request.subject(),
                "body", request.message(),
                "htmlBody", toHtml(request.message()),
                "name", fromName
        ));
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(relayUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        JsonNode result = null;
        try {
            result = objectMapper.readTree(response.body());
        } catch (IOException ignored) {
            // se trata abajo como respuesta inesperada
        }
        if (result == null || !result.isObject()) {
            throw new IOException("Respuesta inesperada del relay (HTTP " + response.statusCode() + ")");
        }
        if (!result.path("ok").asBoolean(false)) {
            String error = result.path("error").asText("");
            throw new IOException(error.isBlank() ? "error desconocido del relay" : error);
        }
    }

    private EmailRecord saveRecord(EmailRequest request, EmailStatus status, String detail) {
        EmailRecord record = new EmailRecord(
                idGenerator.getAndIncrement(),
                request.to(),
                request.subject(),
                request.message(),
                status,
                detail,
                LocalDateTime.now()
        );
        history.add(0, record);
        return record;
    }

    public List<EmailRecord> findAll() {
        synchronized (history) {
            return List.copyOf(history);
        }
    }

    private String toHtml(String text) {
        return "<div style=\"font-family:Arial,sans-serif;font-size:14px\">"
                + HtmlUtils.htmlEscape(text).replace("\n", "<br>")
                + "</div>";
    }
}
