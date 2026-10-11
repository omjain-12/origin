package com.origin.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.origin.domain.entity.ReminderItem;
import com.origin.domain.repository.ReminderItemRepository;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ReminderTool {

    private final ReminderItemRepository reminderItemRepository;

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    public ReminderTool(ReminderItemRepository reminderItemRepository) {
        this.reminderItemRepository = reminderItemRepository;
    }

    public record SetReminderRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("Reminder message text") String message,
        @JsonProperty(required = true) @JsonPropertyDescription("ISO-8601 datetime when to remind (e.g. 2026-10-15T15:50:00Z)") String remindAt
    ) {}

    public record ListRemindersRequest() {}

    public record CancelReminderRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID of the reminder to cancel") String reminderId
    ) {}

    public String setReminder(SetReminderRequest req) {
        try {
            ReminderItem reminder = new ReminderItem();
            reminder.setUserId(userId);
            reminder.setMessage(req.message());
            reminder.setRemindAt(Instant.parse(req.remindAt()));
            ReminderItem saved = reminderItemRepository.save(reminder);
            return "Reminder set with ID: " + saved.getId() + " for " + saved.getRemindAt();
        } catch (Exception e) {
            return "Failed to set reminder: " + e.getMessage();
        }
    }

    public String listActiveReminders(ListRemindersRequest req) {
        try {
            List<ReminderItem> reminders = reminderItemRepository
                .findByUserIdAndFiredFalseOrderByRemindAtAsc(userId);
            if (reminders.isEmpty()) return "No active reminders.";
            return reminders.stream()
                .map(r -> "[%s] %s — at %s".formatted(r.getId(), r.getMessage(), r.getRemindAt()))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list reminders: " + e.getMessage();
        }
    }

    public String cancelReminder(CancelReminderRequest req) {
        try {
            UUID id = UUID.fromString(req.reminderId());
            if (!reminderItemRepository.existsById(id)) return "No reminder found with ID: " + req.reminderId();
            reminderItemRepository.deleteById(id);
            return "Reminder " + req.reminderId() + " cancelled.";
        } catch (Exception e) {
            return "Failed to cancel reminder: " + e.getMessage();
        }
    }

    public List<FunctionCallback> getCallbacks() {
        return List.of(
            FunctionCallbackWrapper.builder(this::setReminder)
                .withName("setReminder")
                .withDescription("Sets a time-based reminder for the user.")
                .withInputType(SetReminderRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::listActiveReminders)
                .withName("listActiveReminders")
                .withDescription("Lists all active reminders for the user.")
                .withInputType(ListRemindersRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::cancelReminder)
                .withName("cancelReminder")
                .withDescription("Cancels a reminder by its UUID.")
                .withInputType(CancelReminderRequest.class)
                .build()
        );
    }
}
