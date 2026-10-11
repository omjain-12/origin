package com.ramlaal.api.dto;

import java.util.UUID;

public record ChatRequest(UUID sessionId, String message) {}
public record ChatResponse(String sessionId, String response) {}
public record CreateSessionResponse(String sessionId, String title) {}
