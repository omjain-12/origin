package com.ramlaal.service;

import com.ramlaal.domain.entity.AlarmItem;
import com.ramlaal.domain.entity.ReminderItem;
import com.ramlaal.domain.repository.AlarmItemRepository;
import com.ramlaal.domain.repository.ReminderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackgroundNotificationDispatcher {

    private final ReminderItemRepository reminderItemRepository;
    private final AlarmItemRepository alarmItemRepository;

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void dispatchDueReminders() {
        List<ReminderItem> due = reminderItemRepository.findByFiredFalseAndRemindAtBefore(Instant.now());
        for (ReminderItem reminder : due) {
            log.info("🔔 REMINDER FIRED [{}]: {}", reminder.getId(), reminder.getMessage());
            reminder.setFired(true);
            reminderItemRepository.save(reminder);
        }
    }

    @Scheduled(fixedDelay = 30_000)
    @Transactional
    public void dispatchDueAlarms() {
        List<AlarmItem> due = alarmItemRepository.findByDismissedFalseAndTriggerAtBefore(Instant.now());
        for (AlarmItem alarm : due) {
            log.info("⏰ ALARM TRIGGERED [{}]: {}", alarm.getId(), alarm.getLabel());
            alarm.setDismissed(true);
            alarmItemRepository.save(alarm);
        }
    }
}
