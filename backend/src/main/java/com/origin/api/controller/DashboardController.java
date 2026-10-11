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
}
