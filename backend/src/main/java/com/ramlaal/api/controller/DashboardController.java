package com.ramlaal.api.controller;

import com.ramlaal.domain.entity.*;
import com.ramlaal.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final CalendarEventRepository calendarEventRepository;
    private final TaskItemRepository taskItemRepository;
    private final ReminderItemRepository reminderItemRepository;
    private final AlarmItemRepository alarmItemRepository;
    private final ScheduledJobItemRepository scheduledJobItemRepository;

    @Value("${ramlaal.memory.user-id:default-user}")
    private String userId;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(Map.of(
            "upcomingEvents", calendarEventRepository
                .findByUserIdAndStartTimeAfterOrderByStartTimeAsc(userId, Instant.now()),
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
}
