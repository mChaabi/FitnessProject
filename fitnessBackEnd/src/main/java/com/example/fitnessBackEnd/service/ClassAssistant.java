package com.example.fitnessBackEnd.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface ClassAssistant {

    record ClassPick(String reply, List<Long> sessionIds) {}

    @SystemMessage(fromResource = "prompts/class-assistant-system.txt")
    @UserMessage("""
        Aujourd'hui : {{today}}

        Demande du membre : {{request}}

        Séances disponibles (id | cours | coach | jour | heure | places libres) :
        {{sessions}}

        Propose les séances.""")
    ClassPick pick(@V("today") String today, @V("request") String request, @V("sessions") String sessions);
}