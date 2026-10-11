package com.origin.api.controller;

import com.origin.api.dto.ChatRequest;
import com.origin.domain.entity.ChatSession;
import com.origin.service.AgentOrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final AgentOrchestratorService orchestrator;

    public ChatController(AgentOrchestratorService orchestrator) {
        this.orchestrator = orchestrator;
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> listSessions() {
        return ResponseEntity.ok(orchestrator.listSessions().stream().map(s -> Map.of(
            "sessionId", s.getId().toString(),
            "title", s.getTitle() != null ? s.getTitle() : "Conversation",
            "createdAt", s.getCreatedAt() != null ? s.getCreatedAt().toString() : "",
            "updatedAt", s.getUpdatedAt() != null ? s.getUpdatedAt().toString() : ""
        )).toList());
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<?> getSessionMessages(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(orchestrator.getMessages(sessionId).stream().map(m -> Map.of(
            "id", m.getId().toString(),
            "role", m.getRole().name().toLowerCase(),
            "content", m.getContent(),
            "createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : ""
        )).toList());
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable UUID sessionId) {
        orchestrator.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> createSession() {
        ChatSession session = orchestrator.createSession();
        return ResponseEntity.ok(Map.of(
            "sessionId", session.getId().toString(),
            "title", session.getTitle()
        ));
    }

    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body("Message cannot be empty.");
        }
        try {
            UUID sessionId = request.sessionId() != null
                ? request.sessionId()
                : orchestrator.createSession().getId();
            String response = orchestrator.chat(sessionId, request.message());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Origin is temporarily unable to process this request. Please try again in a moment.");
        }
    }
}
