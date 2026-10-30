package com.example.fitnessBackEnd.service;

public final class AiTextGuard {
    private AiTextGuard() {}

    /** Devuelve el texto limpio, o null si no es seguro usarlo. */
    public static String clean(String raw) {
        if (raw == null) return null;
        String t = raw.replaceAll("\\s+", " ").trim();
        if (t.length() > 1 && t.startsWith("\"") && t.endsWith("\"")) t = t.substring(1, t.length() - 1).trim();
        if (t.isEmpty() || t.length() > 400) return null;
        if (t.matches(".*[\\d<>@].*")) return null;                       // cifras, HTML o emails
        String low = t.toLowerCase();
        if (low.contains("http") || low.contains("www.")) return null;    // enlaces
        return t;
    }
}
