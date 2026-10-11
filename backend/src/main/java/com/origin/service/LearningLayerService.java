package com.origin.service;

import com.origin.domain.entity.UserMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningLayerService {

    private final ChatModel chatModel;
    private final UserMemoryService userMemoryService;

    private static final String EXTRACTION_PROMPT = """
        Analyze the following conversation and extract any NEW durable personal facts, preferences,
        or recurring habits about the user. Only extract concrete, reusable information.
        
        For each fact, output a line in the format:
        FACT: <fact text> | CATEGORY: <category>
        
        Categories: preference, habit, schedule, personal_info, other
        If no new facts are found, output: NO_NEW_FACTS
        
        Conversation:
        %s
        """;

    private static final Pattern FACT_PATTERN =
        Pattern.compile("FACT:\\s*(.+?)\\s*\\|\\s*CATEGORY:\\s*(\\w+)", Pattern.CASE_INSENSITIVE);

    public void extractAndPersist(String conversationText) {
        try {
            String response = chatModel.call(
                new Prompt(new UserMessage(EXTRACTION_PROMPT.formatted(conversationText)))
            ).getResult().getOutput().getText();

            if (response.contains("NO_NEW_FACTS")) {
                log.debug("Learning layer: no new facts extracted");
                return;
            }

            Matcher matcher = FACT_PATTERN.matcher(response);
            while (matcher.find()) {
                String factText = matcher.group(1).trim();
                String category = matcher.group(2).trim().toLowerCase();
                userMemoryService.saveMemory(factText, category);
                log.info("Learned new fact [{}]: {}", category, factText);
            }
        } catch (Exception e) {
            log.warn("Learning layer extraction failed: {}", e.getMessage());
        }
    }
}
