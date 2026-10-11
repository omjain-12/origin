package com.origin.domain.repository;

import com.origin.domain.entity.TaskItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskItemRepository extends JpaRepository<TaskItem, UUID> {
    List<TaskItem> findByUserIdAndStatusNotOrderByPriorityDescCreatedAtDesc(
        String userId, TaskItem.Status status
    );
}

