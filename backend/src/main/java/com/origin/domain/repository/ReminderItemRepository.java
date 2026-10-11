package com.origin.domain.repository;

import com.origin.domain.entity.ReminderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderItemRepository extends JpaRepository<ReminderItem, UUID> {
    List<ReminderItem> findByUserIdAndFiredFalseOrderByRemindAtAsc(String userId);
    List<ReminderItem> findByUserIdOrderByRemindAtAsc(String userId);
    List<ReminderItem> findByFiredFalseAndRemindAtBefore(Instant now);
}

