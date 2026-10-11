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
        @JsonProperty(required = true) @JsonPropertyDescription("ISO-8601 datetime for the alarm trigger (e.g. YYYY-MM-DDTHH:mm:ssZ), calculated relative to current system time") String triggerAt
    ) {}

    public record ListAlarmsRequest() {}

    public record DismissAlarmRequest(
        @JsonProperty(required = true) @JsonPropertyDescription("UUID or label/name of the alarm to dismiss") String alarmId
    ) {}

    public String setAlarm(SetAlarmRequest req) {
        try {
            AlarmItem alarm = new AlarmItem();
            alarm.setUserId(userId);
            alarm.setLabel(req.label());
            alarm.setTriggerAt(com.origin.util.DateTimeUtil.parseInstant(req.triggerAt()));
            AlarmItem saved = alarmItemRepository.save(alarm);
            return "Alarm set: '" + saved.getLabel() + "' for " + saved.getTriggerAt() + ".";
        } catch (Exception e) {
            return "Failed to set alarm: " + e.getMessage();
        }
    }

    public String listActiveAlarms(ListAlarmsRequest req) {
        try {
            List<AlarmItem> alarms = alarmItemRepository
                .findByUserIdOrderByTriggerAtAsc(userId);
            if (alarms.isEmpty()) return "No alarms found in database.";
            return alarms.stream()
                .map(a -> "- Alarm: '%s' scheduled for %s [Status: %s]".formatted(
                    a.getLabel(),
                    a.getTriggerAt(),
                    a.isDismissed() ? "DISMISSED" : "ACTIVE"))
                .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "Failed to list alarms: " + e.getMessage();
        }
    }

    public String dismissAlarm(DismissAlarmRequest req) {
        try {
            try {
                UUID id = UUID.fromString(req.alarmId());
                return alarmItemRepository.findById(id).map(alarm -> {
                    alarm.setDismissed(true);
                    alarmItemRepository.save(alarm);
                    return "Alarm '" + alarm.getLabel() + "' dismissed.";
                }).orElse("No alarm found with ID: " + req.alarmId());
            } catch (IllegalArgumentException notUuid) {
                List<AlarmItem> matches = alarmItemRepository.findByUserIdOrderByTriggerAtAsc(userId)
                    .stream()
                    .filter(a -> !a.isDismissed() && a.getLabel().toLowerCase().contains(req.alarmId().toLowerCase()))
                    .toList();
                if (!matches.isEmpty()) {
                    AlarmItem alarm = matches.get(0);
                    alarm.setDismissed(true);
                    alarmItemRepository.save(alarm);
                    return "Alarm '" + alarm.getLabel() + "' dismissed.";
                }
                return "No active alarm found matching '" + req.alarmId() + "'.";
            }
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
                .withName("listAlarms")
                .withDescription("Retrieves all alarms from the database (both active and dismissed) with their trigger times and status. Call this tool whenever the user asks to see, check, fetch, or list their alarms.")
                .withInputType(ListAlarmsRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::listActiveAlarms)
                .withName("listActiveAlarms")
                .withDescription("Lists all active and configured alarms for the user.")
                .withInputType(ListAlarmsRequest.class)
                .build(),
            FunctionCallbackWrapper.builder(this::dismissAlarm)
                .withName("dismissAlarm")
                .withDescription("Dismisses (cancels) an alarm by its label/name or UUID.")
                .withInputType(DismissAlarmRequest.class)
                .build()
        );
    }
}
