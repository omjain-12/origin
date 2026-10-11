package com.ramlaal.tool;

import com.ramlaal.domain.entity.ReminderItem;
import com.ramlaal.domain.repository.ReminderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReminderTool {

    private final ReminderItemRepository reminderItemRepository;

    @Value("${ramlaal.memory.user-id:default-user}")
    private String userId;

    @Tool(description = "Sets a time-based reminder. Returns the reminder's ID.")
    public String setReminder(
        @ToolParam(description = "Reminder message text") String message,
        @ToolParam(description = "ISO-8601 datetime when to remind (e.g. 2026-10-15T15:50:00Z)") String remindAt
    ) {
        try {
            ReminderItem reminder = new ReminderItem();
            reminder.setUserId(userId);
            reminder.setMessage(message);
            reminder.setRemindAt(Instant.parse(remindAt));
            ReminderItem saved = reminderItemRepository.save(reminder);
            return "Reminder set with ID: " + saved.getId() + " for " + saved.getRemindAt();
        } catch (Exception e) {
            return "Failed to set reminder: " + e.getMessage();
        }
    }

    @Tool(description = "Lists all active (unfired) reminders for the user.")
    public String listActiveReminders() {
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

    @Tool(description = "Cancels a reminder by its ID.")
    public String cancelReminder(
        @ToolParam(description = "UUID of the reminder to cancel") String reminderId
    ) {
        try {
            UUID id = UUID.fromString(reminderId);
            if (!reminderItemRepository.existsById(id)) return "No reminder found with ID: " + reminderId;
            reminderItemRepository.deleteById(id);
            return "Reminder " + reminderId + " cancelled.";
        } catch (Exception e) {
            return "Failed to cancel reminder: " + e.getMessage();
        }
    }
}
