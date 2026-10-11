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
        @JsonProperty(required = true) @JsonPropertyDescription("ISO-8601 datetime when to remind (e.g. YYYY-MM-DDTHH:mm:ssZ), calculated relative to current system time") String remindAt
    ) {}

    public record ListRemindersRequest() {}

    public record CancelReminderRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID or message/text of the reminder to cancel") String reminderId
    ) {}

    public String setReminder(SetReminderRequest req) {
        try {
            ReminderItem reminder = new ReminderItem();
            reminder.setUserId(userId);
            reminder.setMessage(req.message());
            reminder.setRemindAt(com.origin.util.DateTimeUtil.parseInstant(req.remindAt()));
            ReminderItem saved = reminderItemRepository.save(reminder);
            return "Reminder set: '" + saved.getMessage() + "' for " + saved.getRemindAt() + ".";
        } catch (Exception e) {
            return "Failed to set reminder: " + e.getMessage();
        }
    }

    public String listActiveReminders(ListRemindersRequest req) {
        try {
            List<ReminderItem> reminders = reminderItemRepository
                .findByUserIdOrderByRemindAtAsc(userId);
            if (reminders.isEmpty()) return "No reminders found in database.";
            return reminders.stream()
                .map(r -> "- Reminder: '%s' scheduled for %s [Status: %s]".formatted(
                    r.getMessage(),
                    r.getRemindAt(),
                    r.isFired() ? "COMPLETED/FIRED" : "PENDING/ACTIVE"))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list reminders: " + e.getMessage();
        }
    }

    public String cancelReminder(CancelReminderRequest req) {
        try {
            try {
                UUID id = UUID.fromString(req.reminderId());
                if (!reminderItemRepository.existsById(id)) return "No reminder found with ID: " + req.reminderId();
                reminderItemRepository.deleteById(id);
                return "Reminder cancelled successfully.";
            } catch (IllegalArgumentException notUuid) {
                List<ReminderItem> matches = reminderItemRepository.findByUserIdOrderByRemindAtAsc(userId)
                    .stream()
                    .filter(r -> !r.isFired() && r.getMessage().toLowerCase().contains(req.reminderId().toLowerCase()))
                    .toList();
                if (!matches.isEmpty()) {
                    ReminderItem reminder = matches.get(0);
                    reminderItemRepository.deleteById(reminder.getId());
                    return "Reminder '" + reminder.getMessage() + "' cancelled successfully.";
                }
                return "No pending reminder found matching '" + req.reminderId() + "'.";
            }
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
                .withName("listReminders")
                .withDescription("Retrieves all reminders from the database (both active/pending and past/fired). Call this tool whenever the user asks to see, check, fetch, or list reminders.")
                .withInputType(ListRemindersRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::listActiveReminders)
                .withName("listActiveReminders")
                .withDescription("Lists all active reminders for the user.")
                .withInputType(ListRemindersRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::cancelReminder)
                .withName("cancelReminder")
                .withDescription("Cancels a reminder by its message/text or UUID.")
                .withInputType(CancelReminderRequest.class)
                .build()
        );
    }
}
