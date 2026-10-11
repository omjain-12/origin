package com.origin.service;

import com.origin.domain.entity.AlarmItem;
import com.origin.domain.entity.ReminderItem;
import com.origin.domain.repository.AlarmItemRepository;
import com.origin.domain.repository.ReminderItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BackgroundNotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(BackgroundNotificationDispatcher.class);

    private final ReminderItemRepository reminderItemRepository;
    private final AlarmItemRepository alarmItemRepository;

    public BackgroundNotificationDispatcher(ReminderItemRepository reminderItemRepository,
                                            AlarmItemRepository alarmItemRepository) {
        this.reminderItemRepository = reminderItemRepository;
        this.alarmItemRepository = alarmItemRepository;
    }

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
