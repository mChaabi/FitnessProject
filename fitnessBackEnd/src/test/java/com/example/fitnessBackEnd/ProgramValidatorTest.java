package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.dto.DraftItem;
import com.example.fitnessBackEnd.dto.DraftProgram;
import com.example.fitnessBackEnd.entity.Exercice;
import com.example.fitnessBackEnd.repository.ExerciceRepository;
import com.example.fitnessBackEnd.service.ProgramValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramValidatorTest extends AbstractIntegrationTest {

    @Autowired
    ProgramValidator validator;
    @Autowired
    ExerciceRepository exercices;

    private Long realId() { return exercices.save(Exercice.builder().nom("Squat-" + unique()).build()).getId(); }

    @Test
    void validDraftPasses() {
        Long id = realId();
        var d = new DraftProgram("P", "g", List.of(new DraftItem(id, 1, 3, 10, 60, null)));
        assertTrue(validator.validate(d, Set.of(1)).isEmpty());
    }
    @Test void inventedExerciseIsRejected() {
        var d = new DraftProgram("P", "g", List.of(new DraftItem(999999L, 1, 3, 10, 60, null)));
        assertFalse(validator.validate(d, Set.of(1)).isEmpty());
    }
    @Test void absurdVolumeIsRejected() {
        Long id = realId();
        var d = new DraftProgram("P", "g", List.of(new DraftItem(id, 1, 99, 500, 0, null)));
        assertTrue(validator.validate(d, Set.of(1)).size() >= 3);
    }
    @Test void unrequestedDayIsRejected() {
        Long id = realId();
        var d = new DraftProgram("P", "g", List.of(new DraftItem(id, 5, 3, 10, 60, null)));
        assertFalse(validator.validate(d, Set.of(1)).isEmpty());
    }
    @Test void missingRequestedDayIsRejected() {
        Long id = realId();
        var d = new DraftProgram("P", "g", List.of(new DraftItem(id, 1, 3, 10, 60, null)));
        assertFalse(validator.validate(d, Set.of(1, 3)).isEmpty());
    }
}
