package com.correos.api.controller;

import com.correos.api.dto.EmailRequest;
import com.correos.api.dto.EmailResponse;
import com.correos.api.model.EmailRecord;
import com.correos.api.model.EmailStatus;
import com.correos.api.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> send(@Valid @RequestBody EmailRequest request) {
        EmailRecord record = emailService.send(request);
        boolean ok = record.status() == EmailStatus.SENT;
        return ResponseEntity
                .status(ok ? HttpStatus.OK : HttpStatus.BAD_GATEWAY)
                .body(new EmailResponse(ok, record.detail(), record));
    }

    @GetMapping
    public List<EmailRecord> history() {
        return emailService.findAll();
    }
}
