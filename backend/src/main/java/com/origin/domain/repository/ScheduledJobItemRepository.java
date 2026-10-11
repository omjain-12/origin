package com.origin.domain.repository;

import com.origin.domain.entity.ScheduledJobItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduledJobItemRepository extends JpaRepository<ScheduledJobItem, UUID> {
    List<ScheduledJobItem> findByUserIdAndStatusOrderByCreatedAtDesc(
        String userId, ScheduledJobItem.Status status
    );
    List<ScheduledJobItem> findByUserIdOrderByCreatedAtDesc(String userId);
}

