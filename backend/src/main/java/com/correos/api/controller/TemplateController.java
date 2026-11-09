package com.correos.api.controller;

import com.correos.api.dto.TemplateRequest;
import com.correos.api.model.EmailTemplate;
import com.correos.api.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public List<EmailTemplate> findAll() {
        return templateService.findAll();
    }

    @GetMapping("/{id}")
    public EmailTemplate findById(@PathVariable Long id) {
        return templateService.findById(id);
    }

    @PostMapping
    public ResponseEntity<EmailTemplate> create(@Valid @RequestBody TemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.create(request));
    }

    @PutMapping("/{id}")
    public EmailTemplate update(@PathVariable Long id, @Valid @RequestBody TemplateRequest request) {
        return templateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
