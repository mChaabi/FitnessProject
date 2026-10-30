package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.Fact;
import com.example.fitnessBackEnd.entity.EmailMessage;
import com.example.fitnessBackEnd.enums.EmailType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailRenderer {

    public record Rendered(String subject, String html, String text, boolean ai) {}

    private final TemplateEngine engine;
    private final ParagraphService paragraphs;
    @Value("${app.frontend-url}") private String frontend;

    public Rendered render(EmailMessage m, String unsubscribeUrl) {
        EmailType type = m.getType();
        List<Fact> facts = Facts.parse(m.getFactsText());
        ParagraphService.Result p = paragraphs.paragraph(type, facts);
        String cta = frontend + type.getCtaPath();
        String name = m.getRecipientName() == null ? "" : m.getRecipientName();

        Context ctx = new Context(Locale.FRENCH);
        ctx.setVariable("subject", type.getSubject());
        ctx.setVariable("prenom", name);
        ctx.setVariable("paragraph", p.text());
        ctx.setVariable("facts", facts);
        ctx.setVariable("ctaLabel", type.getCtaLabel());
        ctx.setVariable("ctaUrl", cta);
        ctx.setVariable("unsubscribeUrl", unsubscribeUrl);
        String html = engine.process("mail/notification", ctx);

        StringBuilder text = new StringBuilder("Bonjour " + name + ",\n\n" + p.text() + "\n\n");
        facts.forEach(f -> text.append(f.label()).append(" : ").append(f.value()).append('\n'));
        text.append('\n').append(type.getCtaLabel()).append(" : ").append(cta).append("\n\nL'équipe FitForge");
        if (unsubscribeUrl != null) text.append("\n\nNe plus recevoir ces emails : ").append(unsubscribeUrl);

        return new Rendered(type.getSubject(), html, text.toString(), p.ai());
    }
}
