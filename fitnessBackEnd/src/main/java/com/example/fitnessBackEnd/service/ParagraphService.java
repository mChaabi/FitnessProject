package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.Fact;
import com.example.fitnessBackEnd.enums.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParagraphService {

    public record Result(String text, boolean ai) {}

    private final ObjectProvider<EmailCopywriter> copywriter;     // vacío si la IA está desactivada

    private final Map<String, String> cache = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<String, String> e) { return size() > 200; }
            });

    public Result paragraph(EmailType type, List<Fact> facts) {
        EmailCopywriter ai = copywriter.getIfAvailable();
        if (ai != null) {
            String factsText = Facts.toText(facts);
            String key = type + "|" + factsText;
            String cached = cache.get(key);
            if (cached != null) return new Result(cached, true);
            try {
                String clean = AiTextGuard.clean(ai.write(type.getTask(), factsText));
                if (clean != null) { cache.put(key, clean); return new Result(clean, true); }
                log.warn("Respuesta de IA descartada por el guardián (tipo {})", type);
            } catch (Exception e) {
                log.warn("IA no disponible, se usa el texto de respaldo: {}", e.getMessage());
            }
        }
        return new Result(type.getFallback(), false);
    }
}
