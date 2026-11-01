package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.DraftProgram;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ProgramDrafter {

    @SystemMessage(fromResource = "prompts/program-system.txt")
    @UserMessage("""
        Profil (anonymisé) : {{profile}}

        Catalogue (id : nom, catégorie) :
        {{catalog}}

        Erreurs de ta version précédente à corriger : {{feedback}}

        Propose le programme.""")
    DraftProgram draft(@V("profile") String profile, @V("catalog") String catalog, @V("feedback") String feedback);
}