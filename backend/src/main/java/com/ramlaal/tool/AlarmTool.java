package com.ramlaal.tool;

import com.ramlaal.domain.entity.AlarmItem;
import com.ramlaal.domain.repository.AlarmItemRepository;
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
public class AlarmTool {

    private final AlarmItemRepository alarmItemRepository;

    @Value("${ramlaal.memory.user-id:default-user}")
    private String userId;

    @Tool(description = "Sets a one-shot alarm at a specific datetime. Returns the alarm's ID.")
    public String setAlarm(
        @ToolParam(description = "Label/description for the alarm") String label,
        @ToolParam(description = "ISO-8601 datetime for the alarm trigger (e.g. 2026-10-15T07:00:00Z)") String triggerAt
    ) {
        try {
            AlarmItem alarm = new AlarmItem();
            alarm.setUserId(userId);
            alarm.setLabel(label);
            alarm.setTriggerAt(Instant.parse(triggerAt));
            AlarmItem saved = alarmItemRepository.save(alarm);
            return "Alarm set with ID: " + saved.getId() + " — triggers at " + saved.getTriggerAt();
        } catch (Exception e) {
            return "Failed to set alarm: " + e.getMessage();
        }
    }

    @Tool(description = "Lists all active (undismissed) alarms for the user.")
    public String listActiveAlarms() {
        try {
            List<AlarmItem> alarms = alarmItemRepository
                .findByUserIdAndDismissedFalseOrderByTriggerAtAsc(userId);
            if (alarms.isEmpty()) return "No active alarms.";
            return alarms.stream()
                .map(a -> "[%s] %s — triggers at %s".formatted(a.getId(), a.getLabel(), a.getTriggerAt()))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list alarms: " + e.getMessage();
        }
    }

    @Tool(description = "Dismisses (cancels) an alarm by its ID.")
    public String dismissAlarm(
        @ToolParam(description = "UUID of the alarm to dismiss") String alarmId
    ) {
        try {
            UUID id = UUID.fromString(alarmId);
            return alarmItemRepository.findById(id).map(alarm -> {
                alarm.setDismissed(true);
                alarmItemRepository.save(alarm);
                return "Alarm '" + alarm.getLabel() + "' dismissed.";
            }).orElse("No alarm found with ID: " + alarmId);
        } catch (Exception e) {
            return "Failed to dismiss alarm: " + e.getMessage();
        }
    }
}

