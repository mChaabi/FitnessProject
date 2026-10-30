package com.example.fitnessBackEnd.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface EmailCopywriter {

    @SystemMessage(fromResource = "prompts/email-system.txt")
    @UserMessage("Tâche : {{task}}\n\nFaits :\n{{facts}}\n\nRédige le paragraphe.")
    String write(@V("task") String task, @V("facts") String facts);
}
