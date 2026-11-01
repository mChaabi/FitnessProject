package com.example.fitnessBackEnd.service;

import com.example.fitnessBackEnd.dto.DraftProgram;
import lombok.RequiredArgsConstructor;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Component
public class ProgramDraftEngine {

    private final ProgramDrafter drafter;
    private final ProgramValidator validator;

    public ProgramDraftEngine(ProgramDrafter drafter, ProgramValidator validator) {
        this.drafter = drafter;
        this.validator = validator;
    }
    @Value("${app.agents.program.max-attempts:3}") private int maxAttempts;

    // ---------- estado ----------
    public static class State extends AgentState {
        public State(Map<String, Object> init) { super(init); }
        public String profile()  { return this.<String>value("profile").orElse(""); }
        public String catalog()  { return this.<String>value("catalog").orElse(""); }
        @SuppressWarnings("unchecked")
        public Set<Integer> days() { return (Set<Integer>) value("days").orElse(Set.of()); }
        public int attempts()    { return this.<Integer>value("attempts").orElse(0); }
        public DraftProgram draft() { return this.<DraftProgram>value("draft").orElse(null); }
        @SuppressWarnings("unchecked")
        public List<String> errors() { return (List<String>) value("errors").orElse(List.of()); }
    }

    // ---------- nodos: lógica pura ----------
    Map<String, Object> generate(State s) {
        ProgramDrafter ai = drafter;
        if (ai == null) return Map.of("errors", List.of("IA désactivée"), "attempts", maxAttempts);
        String feedback = s.errors().isEmpty() ? "Aucune" : String.join(" ; ", s.errors());
        try {
            DraftProgram d = ai.draft(s.profile(), s.catalog(), feedback);
            return Map.of("draft", d, "attempts", s.attempts() + 1);
        } catch (Exception e) {
            return Map.of("errors", List.of("Échec de génération"), "attempts", s.attempts() + 1);
        }
    }

    Map<String, Object> validate(State s) {
        return Map.of("errors", validator.validate(s.draft(), s.days()));
    }

    String route(State s) {
        if (s.errors().isEmpty()) return "ok";
        return s.attempts() < maxAttempts ? "retry" : "fail";
    }

    // ---------- el grafo: solo conecta las funciones ----------
    public CompiledGraph<State> build() throws GraphStateException {
        return new StateGraph<>(Map.<String, Channel<?>>of(), State::new)
                .addNode("generate", node_async(this::generate))
                .addNode("validate", node_async(this::validate))
                .addEdge(StateGraph.START, "generate")
                .addEdge("generate", "validate")
                .addConditionalEdges("validate", edge_async(this::route),
                        Map.of("ok", StateGraph.END, "retry", "generate", "fail", StateGraph.END))
                .compile();
    }

    /** Ejecuta el grafo y devuelve el estado final. */
    public State run(String profile, String catalog, Set<Integer> days) throws GraphStateException {
        Map<String, Object> input = Map.of("profile", profile, "catalog", catalog, "days", days);
        return build().invoke(input).orElseThrow(() -> new IllegalStateException("El grafo no devolvió estado"));
    }
}
