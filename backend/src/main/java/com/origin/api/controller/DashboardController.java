package com.origin.api.controller;

import com.origin.domain.entity.*;
import com.origin.domain.repository.*;
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

    @Value("${Origin.memory.user-id:default-user}")
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

