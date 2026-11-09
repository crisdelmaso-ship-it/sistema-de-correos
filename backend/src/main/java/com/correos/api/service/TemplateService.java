package com.correos.api.service;

import com.correos.api.dto.TemplateRequest;
import com.correos.api.exception.ResourceNotFoundException;
import com.correos.api.model.EmailTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TemplateService {

    private final Map<Long, EmailTemplate> templates = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public TemplateService() {
        create(new TemplateRequest("Bienvenida", "¡Bienvenido a nuestra plataforma!",
                "Hola,\n\nGracias por registrarte. Estamos felices de tenerte con nosotros.\n\nSaludos,\nEl equipo"));
        create(new TemplateRequest("Recordatorio", "Recordatorio importante",
                "Hola,\n\nTe recordamos que tienes una actividad pendiente.\n\nSaludos."));
        create(new TemplateRequest("Agradecimiento", "¡Gracias!",
                "Hola,\n\nQueremos agradecerte por tu confianza.\n\nUn saludo cordial."));
    }

    public List<EmailTemplate> findAll() {
        return templates.values().stream()
                .sorted(Comparator.comparing(EmailTemplate::id))
                .toList();
    }

    public EmailTemplate findById(Long id) {
        EmailTemplate template = templates.get(id);
        if (template == null) {
            throw new ResourceNotFoundException("Plantilla no encontrada: " + id);
        }
        return template;
    }

    public EmailTemplate create(TemplateRequest request) {
        long id = idGenerator.getAndIncrement();
        EmailTemplate template = new EmailTemplate(id, request.name(), request.subject(), request.body());
        templates.put(id, template);
        return template;
    }

    public EmailTemplate update(Long id, TemplateRequest request) {
        findById(id);
        EmailTemplate template = new EmailTemplate(id, request.name(), request.subject(), request.body());
        templates.put(id, template);
        return template;
    }

    public void delete(Long id) {
        findById(id);
        templates.remove(id);
    }
}
