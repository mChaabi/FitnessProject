package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.dto.DraftItem;
import com.example.fitnessBackEnd.dto.DraftProgram;
import com.example.fitnessBackEnd.entity.Exercice;
import com.example.fitnessBackEnd.repository.ExerciceRepository;
import com.example.fitnessBackEnd.service.ProgramDraftEngine;
import com.example.fitnessBackEnd.service.ProgramDrafter;
import com.example.fitnessBackEnd.service.ProgramValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProgramDraftEngineTest extends AbstractIntegrationTest {

    @Autowired
    ProgramValidator validator;
    @Autowired
    ExerciceRepository exercices;

    @Test
    void retriesWithFeedbackThenSucceeds() throws Exception {
        Long id = exercices.save(Exercice.builder().nom("Press-" + unique()).build()).getId();
        List<String> feedbacks = new ArrayList<>();

        ProgramDrafter fake = (profile, catalog, feedback) -> {
            feedbacks.add(feedback);
            return feedbacks.size() == 1
                    ? new DraftProgram("P", "g", List.of(new DraftItem(424242L, 1, 3, 10, 60, null)))   // inventado
                    : new DraftProgram("P", "g", List.of(new DraftItem(id, 1, 3, 10, 60, null)));
        };
        var provider = new StaticObjectProvider<>(fake);
        var engine = new ProgramDraftEngine(fake, validator);
        ReflectionTestUtils.setField(engine, "maxAttempts", 3);

        var state = engine.run("profil", "catalogue", Set.of(1));

        assertTrue(state.errors().isEmpty());
        assertEquals(2, state.attempts());
        assertEquals("Aucune", feedbacks.get(0));
        assertTrue(feedbacks.get(1).contains("n'existe pas"));     // el 2º intento recibió el error concreto
    }

    @Test void givesUpAfterMaxAttempts() throws Exception {
        ProgramDrafter alwaysBad = (p, c, f) -> new DraftProgram("P", "g", List.of());
        var engine = new ProgramDraftEngine(alwaysBad, validator);
        ReflectionTestUtils.setField(engine, "maxAttempts", 3);

        var state = engine.run("profil", "catalogue", Set.of(1));

        assertFalse(state.errors().isEmpty());
        assertEquals(3, state.attempts());
    }
}
