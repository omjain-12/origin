package com.origin.service;

import com.origin.domain.entity.ChatMessage;
import com.origin.domain.entity.ChatSession;
import com.origin.domain.entity.UserMemory;
import com.origin.domain.repository.ChatMessageRepository;
import com.origin.domain.repository.ChatSessionRepository;
import com.origin.tool.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgentOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(AgentOrchestratorService.class);

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

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    public AgentOrchestratorService(
            ChatModel chatModel,
            ChatSessionRepository sessionRepository,
            ChatMessageRepository messageRepository,
            QueryExpansionService queryExpansionService,
            VectorRetrievalService vectorRetrievalService,
            UserMemoryService userMemoryService,
            LearningLayerService learningLayerService,
            CalendarTool calendarTool,
            TaskTool taskTool,
            ReminderTool reminderTool,
            AlarmTool alarmTool,
            SchedulerTool schedulerTool) {
        this.chatModel = chatModel;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.queryExpansionService = queryExpansionService;
        this.vectorRetrievalService = vectorRetrievalService;
        this.userMemoryService = userMemoryService;
        this.learningLayerService = learningLayerService;
        this.calendarTool = calendarTool;
        this.taskTool = taskTool;
        this.reminderTool = reminderTool;
        this.alarmTool = alarmTool;
        this.schedulerTool = schedulerTool;
    }

    private static final String SYSTEM_PROMPT = """
        You are Origin, a proactive and intelligent personal AI assistant. You have access to the
        user's calendar, tasks, reminders, alarms, and scheduled jobs.

        ## Current Temporal Context:
        - Current system datetime: {current_time}
        - Current day of the week: {day_of_week}
        - When the user asks to schedule, remind, or create items relative to now (e.g. 'in 30 minutes', 'tomorrow at 9 AM', 'this Thursday', 'today at 4 PM'), you MUST calculate the target ISO-8601 datetime relative to the current system datetime above.

        ## Critical Instructions on Database Tools vs Memory:
        - Alarms, reminders, calendar events, tasks, and scheduled jobs are stored in the database, NOT in the memory context or document context below.
        - Whenever the user asks to check, list, view, fetch, or inquire about their reminders, alarms, schedule/calendar, tasks, or jobs (e.g. 'what are my reminders?', 'fetch all alarms', 'show my schedule', 'what tasks do I have?'), you MUST invoke the corresponding tool (`listReminders`, `listAlarms`, `listCalendarEvents`, `listTasks`, `listJobs`) to query the database.
        - NEVER assume or answer that there are no items based solely on empty vector memory or document context without executing the tool first.
        - When the user asks to schedule, remind, create, complete, or delete items, actively invoke the appropriate tools.

        ## User Interaction & Privacy Directives:
        - NEVER expose internal database IDs, UUIDs, session IDs, or technical identifiers to the user in your responses. Always refer to items naturally by their title, label, or time (e.g. 'Buy groceries', 'your 7 AM alarm', 'team sync at 3 PM').
        - Never ask the user to provide a UUID or technical ID to complete, delete, or dismiss items. Use the title or message when invoking tools.
        - When confirming actions, confirm clearly and conversationally (e.g. "I've added 'Buy groceries' to your tasks with High priority." or "I've set an alarm for 7:00 AM.").

        ## Relevant User Context (from memory):
        {memories}

        ## Relevant Document Context (from knowledge base):
        {rag_context}

        Always be concise, helpful, and proactive. When presenting items from tools, format them cleanly using Markdown. When you create or modify something, confirm what was done. Respect the user's stated preferences.
        """;

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
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        String currentTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))
            + " (" + now.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy h:mm a")) + ")";
        String dayOfWeek = now.getDayOfWeek().toString();

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT
            .replace("{current_time}", currentTime)
            .replace("{day_of_week}", dayOfWeek)
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
        List<FunctionCallback> callbacks = new ArrayList<>();
        callbacks.addAll(calendarTool.getCallbacks());
        callbacks.addAll(taskTool.getCallbacks());
        callbacks.addAll(reminderTool.getCallbacks());
        callbacks.addAll(alarmTool.getCallbacks());
        callbacks.addAll(schedulerTool.getCallbacks());

        var chatOptions = OpenAiChatOptions.builder()
            .model("zai.glm-5")
            .functionCallbacks(callbacks)
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

        // Update title if it is default
        if ("New Conversation".equals(session.getTitle()) && !userInput.isBlank()) {
            String newTitle = userInput.length() > 36 ? userInput.substring(0, 36) + "..." : userInput;
            session.setTitle(newTitle);
            sessionRepository.save(session);
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

    public List<ChatSession> listSessions() {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public List<ChatMessage> getMessages(UUID sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public void deleteSession(UUID sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}
