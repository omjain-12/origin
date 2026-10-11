package com.ramlaal.service;

import com.ramlaal.domain.entity.ChatMessage;
import com.ramlaal.domain.entity.ChatSession;
import com.ramlaal.domain.entity.UserMemory;
import com.ramlaal.domain.repository.ChatMessageRepository;
import com.ramlaal.domain.repository.ChatSessionRepository;
import com.ramlaal.tool.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentOrchestratorService {

    private final ChatModel chatModel;
    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final QueryExpansionService queryExpansionService;
    private final VectorRetrievalService vectorRetrievalService;
    private final UserMemoryService userMemoryService;
    private final LearningLayerService learningLayerService;
    private final CalendarTool calendarTool;
    private final TaskTool taskTool;
    private final ReminderTool reminderTool;
    private final AlarmTool alarmTool;
    private final SchedulerTool schedulerTool;

    @Value("${ramlaal.memory.user-id:default-user}")
    private String userId;

    @Value("${ramlaal.models.planning:openai/zai.glm-5}")
    private String planningModel;

    private static final String SYSTEM_PROMPT = """
        You are RamLaal, a proactive and intelligent personal AI assistant. You have access to the
        user's calendar, tasks, reminders, alarms, and scheduled jobs. Use tools actively when the
        user asks to schedule, remind, create, or manage items.
        
        ## Relevant User Context (from memory):
        {memories}
        
        ## Relevant Document Context (from knowledge base):
        {rag_context}
        
        Always be concise, helpful, and proactive. When you create or modify something, confirm
        what was done. Respect the user's stated preferences.
        """;

    @Transactional
    public String chat(UUID sessionId, String userInput) {
        ChatSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        // 1. Retrieve relevant memories
        List<UserMemory> memories = userMemoryService.findRelevantMemories(userInput, 5);
        String memoriesText = memories.stream()
            .map(m -> "- [%s] %s".formatted(m.getCategory(), m.getFactText()))
            .collect(Collectors.joining("\n"));

        // 2. Query expansion + RAG retrieval
        List<String> expandedQueries = queryExpansionService.expand(userInput);
        String ragContext = vectorRetrievalService.retrieveContext(expandedQueries);

        // 3. Build conversation history
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT
            .replace("{memories}", memoriesText.isBlank() ? "None yet." : memoriesText)
            .replace("{rag_context}", ragContext.isBlank() ? "No relevant documents found." : ragContext)));

        messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).forEach(msg -> {
            if (msg.getRole() == ChatMessage.Role.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else if (msg.getRole() == ChatMessage.Role.ASSISTANT) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        });
        messages.add(new UserMessage(userInput));

        // 4. Persist user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setRole(ChatMessage.Role.USER);
        userMsg.setContent(userInput);
        messageRepository.save(userMsg);

        // 5. Call LLM with tools (using OpenAI-compatible options)
        var toolCallbacks = ToolCallbackProvider.from(
            calendarTool, taskTool, reminderTool, alarmTool, schedulerTool
        );
        var chatOptions = OpenAiChatOptions.builder()
            .model(planningModel)
            .toolCallbacks(toolCallbacks)
            .build();

        String assistantResponse = chatModel.call(new Prompt(messages, chatOptions))
            .getResult().getOutput().getText();

        // 6. Persist assistant response
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSession(session);
        assistantMsg.setRole(ChatMessage.Role.ASSISTANT);
        assistantMsg.setContent(assistantResponse);
        messageRepository.save(assistantMsg);

        // 7. Post-interaction learning
        String conversationSnippet = "User: " + userInput + "\nAssistant: " + assistantResponse;
        try {
            learningLayerService.extractAndPersist(conversationSnippet);
        } catch (Exception e) {
            log.warn("Learning layer failed silently: {}", e.getMessage());
        }

        return assistantResponse;
    }

    @Transactional
    public ChatSession createSession() {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle("New Conversation");
        return sessionRepository.save(session);
    }
}
