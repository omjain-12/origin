package com.origin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryExpansionService {

    private static final Logger log = LoggerFactory.getLogger(QueryExpansionService.class);

    private final ChatModel chatModel;

    public QueryExpansionService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    private static final String EXPANSION_PROMPT = """
        You are a query expansion assistant. Given the user's question below, produce exactly 3 alternative
        search queries that capture different aspects or phrasings of the same intent.
        Output ONLY a numbered list (1. ... 2. ... 3. ...), no explanation.
        
        User question: %s
        """;

    public List<String> expand(String userQuery) {
        try {
            String rawResponse = chatModel.call(
                new Prompt(new UserMessage(EXPANSION_PROMPT.formatted(userQuery)))
            ).getResult().getOutput().getText();

            return rawResponse.lines()
                .map(line -> line.replaceAll("^\\d+\\.\\s*", "").trim())
                .filter(line -> !line.isBlank())
                .limit(3)
                .toList();
        } catch (Exception e) {
            log.warn("Query expansion failed, returning original query: {}", e.getMessage());
            return List.of(userQuery);
        }
    }
}
