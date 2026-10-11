package com.origin.api.controller;

import com.origin.domain.entity.*;
import com.origin.domain.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CalendarEventRepository calendarEventRepository;
    private final TaskItemRepository taskItemRepository;
    private final ReminderItemRepository reminderItemRepository;
    private final AlarmItemRepository alarmItemRepository;
    private final ScheduledJobItemRepository scheduledJobItemRepository;

    @Value("${Origin.memory.user-id:default-user}")
    private String userId;

    public DashboardController(CalendarEventRepository calendarEventRepository,
                               TaskItemRepository taskItemRepository,
                               ReminderItemRepository reminderItemRepository,
                               AlarmItemRepository alarmItemRepository,
                               ScheduledJobItemRepository scheduledJobItemRepository) {
        this.calendarEventRepository = calendarEventRepository;
        this.taskItemRepository = taskItemRepository;
        this.reminderItemRepository = reminderItemRepository;
        this.alarmItemRepository = alarmItemRepository;
        this.scheduledJobItemRepository = scheduledJobItemRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(Map.of(
            "upcomingEvents", calendarEventRepository
                .findByUserIdOrderByStartTimeDesc(userId),
            "activeTasks", taskItemRepository
                .findByUserIdAndStatusNotOrderByPriorityDescCreatedAtDesc(userId, TaskItem.Status.CANCELLED),
            "activeReminders", reminderItemRepository
                .findByUserIdAndFiredFalseOrderByRemindAtAsc(userId),
            "activeAlarms", alarmItemRepository
                .findByUserIdAndDismissedFalseOrderByTriggerAtAsc(userId),
            "scheduledJobs", scheduledJobItemRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, ScheduledJobItem.Status.ACTIVE)
        ));
    }

    @PatchMapping("/reminders/{id}/dismiss")
    public ResponseEntity<Void> dismissReminder(@PathVariable("id") java.util.UUID id) {
        reminderItemRepository.findById(id).ifPresent(r -> {
            r.setFired(true);
            reminderItemRepository.save(r);
        });
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/alarms/{id}/dismiss")
    public ResponseEntity<Void> dismissAlarm(@PathVariable("id") java.util.UUID id) {
        alarmItemRepository.findById(id).ifPresent(a -> {
            a.setDismissed(true);
            alarmItemRepository.save(a);
        });
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tasks/{id}/toggle")
    public ResponseEntity<Void> toggleTask(@PathVariable("id") java.util.UUID id) {
        taskItemRepository.findById(id).ifPresent(t -> {
            if (t.getStatus() == TaskItem.Status.COMPLETED) {
                t.setStatus(TaskItem.Status.PENDING);
            } else {
                t.setStatus(TaskItem.Status.COMPLETED);
            }
            taskItemRepository.save(t);
        });
        return ResponseEntity.noContent().build();
    }

    public record CreateEventDto(String title, String description, String startTime, String endTime, String location) {}
    public record CreateTaskDto(String title, String description, String priority, String status, String dueDate) {}
    public record CreateReminderDto(String message, String remindAt) {}
    public record CreateAlarmDto(String label, String triggerAt) {}

    @PostMapping("/events")
    public ResponseEntity<CalendarEvent> createEvent(@RequestBody CreateEventDto dto) {
        CalendarEvent event = new CalendarEvent();
        event.setUserId(userId);
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setStartTime(com.origin.util.DateTimeUtil.parseInstant(dto.startTime()));
        event.setEndTime(com.origin.util.DateTimeUtil.parseInstant(dto.endTime()));
        event.setLocation(dto.location());
        return ResponseEntity.ok(calendarEventRepository.save(event));
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskItem> createTask(@RequestBody CreateTaskDto dto) {
        TaskItem task = new TaskItem();
        task.setUserId(userId);
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        if (dto.priority() != null) {
            try {
                task.setPriority(TaskItem.Priority.valueOf(dto.priority().toUpperCase()));
            } catch (Exception ignored) {}
        }
        if (dto.status() != null) {
            try {
                task.setStatus(TaskItem.Status.valueOf(dto.status().toUpperCase()));
            } catch (Exception ignored) {}
        }
        if (dto.dueDate() != null && !dto.dueDate().isBlank()) {
            task.setDueDate(com.origin.util.DateTimeUtil.parseInstant(dto.dueDate()));
        }
        return ResponseEntity.ok(taskItemRepository.save(task));
    }

    @PostMapping("/reminders")
    public ResponseEntity<ReminderItem> createReminder(@RequestBody CreateReminderDto dto) {
        ReminderItem reminder = new ReminderItem();
        reminder.setUserId(userId);
        reminder.setMessage(dto.message());
        reminder.setRemindAt(com.origin.util.DateTimeUtil.parseInstant(dto.remindAt()));
        return ResponseEntity.ok(reminderItemRepository.save(reminder));
    }

    @PostMapping("/alarms")
    public ResponseEntity<AlarmItem> createAlarm(@RequestBody CreateAlarmDto dto) {
        AlarmItem alarm = new AlarmItem();
        alarm.setUserId(userId);
        alarm.setLabel(dto.label());
        alarm.setTriggerAt(com.origin.util.DateTimeUtil.parseInstant(dto.triggerAt()));
        return ResponseEntity.ok(alarmItemRepository.save(alarm));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearAll() {
        calendarEventRepository.deleteAll();
        taskItemRepository.deleteAll();
        reminderItemRepository.deleteAll();
        alarmItemRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
