package com.example.fitnessBackEnd.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface DataAnalyst {

    record Insight(String title, String detail, String severity, List<String> metricKeys) {}
    record Analysis(String summary, List<Insight> insights, List<String> actions) {}

    @SystemMessage(fromResource = "prompts/analyst-system.txt")
    @UserMessage("""
        Date : {{today}}

        Question du gérant : {{question}}

        Métriques (clé | libellé | valeur) :
        {{facts}}

        Rédige l'analyse.""")
    Analysis analyze(@V("today") String today, @V("question") String question, @V("facts") String facts);
}