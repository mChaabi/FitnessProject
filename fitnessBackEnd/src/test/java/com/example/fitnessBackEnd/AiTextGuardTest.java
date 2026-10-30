package com.example.fitnessBackEnd;

import com.example.fitnessBackEnd.service.AiTextGuard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AiTextGuardTest {
    @Test
    void acceptsPlainSentence() {
        assertEquals("Nous avons hâte de vous voir.", AiTextGuard.clean("  \"Nous avons hâte de vous voir.\" "));
    }
    @Test
    void rejectsDigits()   { assertNull(AiTextGuard.clean("Rendez-vous à 18h.")); }
    @Test
    void rejectsLinks()    { assertNull(AiTextGuard.clean("Visitez http://evil.example")); }
    @Test
    void rejectsHtml()     { assertNull(AiTextGuard.clean("<b>Bonjour</b>")); }
    @Test
    void rejectsTooLong()  { assertNull(AiTextGuard.clean("a".repeat(401))); }
    @Test
    void rejectsEmpty()    { assertNull(AiTextGuard.clean("   ")); }
}
