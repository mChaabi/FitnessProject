package com.example.fitnessBackEnd.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true")
public class AiConfig {

    @Bean
    ChatModel chatModel(@Value("${app.ai.api-key}") String apiKey,
                        @Value("${app.ai.model}") String model,
                        @Value("${app.ai.timeout-seconds:20}") int timeout) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("GEMINI_API_KEY no definida");
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey).modelName(model)
                .temperature(0.4)                       // poca creatividad: texto sobrio y estable
                .maxOutputTokens(200)                   // tope de coste y de longitud
                .timeout(Duration.ofSeconds(timeout))
                .maxRetries(1)
                .build();
    }

    @Bean
    EmailCopywriter emailCopywriter(ChatModel model) {
        return AiServices.create(EmailCopywriter.class, model);
    }
}
