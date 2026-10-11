package com.origin.api.dto;

import java.util.UUID;

public record ChatRequest(UUID sessionId, String message) {}
