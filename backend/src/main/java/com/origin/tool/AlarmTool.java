package com.origin.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.origin.domain.entity.AlarmItem;
import com.origin.domain.repository.AlarmItemRepository;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AlarmTool {

    private final AlarmItemRepository alarmItemRepository;

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    public AlarmTool(AlarmItemRepository alarmItemRepository) {
        this.alarmItemRepository = alarmItemRepository;
    }

    public record SetAlarmRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("Label/description for the alarm") String label,
        @JsonProperty(required = true) @JsonPropertyDescription("ISO-8601 datetime for the alarm trigger (e.g. 2026-10-15T07:00:00Z)") String triggerAt
    ) {}

    public record ListAlarmsRequest() {}

    public record DismissAlarmRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID of the alarm to dismiss") String alarmId
    ) {}

    public String setAlarm(SetAlarmRequest req) {
        try {
            AlarmItem alarm = new AlarmItem();
            alarm.setUserId(userId);
            alarm.setLabel(req.label());
            alarm.setTriggerAt(Instant.parse(req.triggerAt()));
            AlarmItem saved = alarmItemRepository.save(alarm);
            return "Alarm set with ID: " + saved.getId() + " — triggers at " + saved.getTriggerAt();
        } catch (Exception e) {
            return "Failed to set alarm: " + e.getMessage();
        }
    }

    public String listActiveAlarms(ListAlarmsRequest req) {
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

    public String dismissAlarm(DismissAlarmRequest req) {
        try {
            UUID id = UUID.fromString(req.alarmId());
            return alarmItemRepository.findById(id).map(alarm -> {
                alarm.setDismissed(true);
                alarmItemRepository.save(alarm);
                return "Alarm '" + alarm.getLabel() + "' dismissed.";
            }).orElse("No alarm found with ID: " + req.alarmId());
        } catch (Exception e) {
            return "Failed to dismiss alarm: " + e.getMessage();
        }
    }

    public List<FunctionCallback> getCallbacks() {
        return List.of(
            FunctionCallbackWrapper.builder(this::setAlarm)
                .withName("setAlarm")
                .withDescription("Sets a one-shot alarm at a specific datetime.")
                .withInputType(SetAlarmRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::listActiveAlarms)
                .withName("listActiveAlarms")
                .withDescription("Lists all active (undismissed) alarms for the user.")
                .withInputType(ListAlarmsRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::dismissAlarm)
                .withName("dismissAlarm")
                .withDescription("Dismisses (cancels) an alarm by its UUID.")
                .withInputType(DismissAlarmRequest.class)
                .build()
        );
    }
}
