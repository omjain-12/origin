package com.ramlaal.domain.repository;

import com.ramlaal.domain.entity.AlarmItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlarmItemRepository extends JpaRepository<AlarmItem, UUID> {
    List<AlarmItem> findByUserIdAndDismissedFalseOrderByTriggerAtAsc(String userId);
    List<AlarmItem> findByDismissedFalseAndTriggerAtBefore(Instant now);
}

