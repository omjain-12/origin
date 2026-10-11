package com.origin.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scheduled_jobs")
@Data
@NoArgsConstructor
public class ScheduledJobItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String jobName;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Standard 5-field cron expression e.g. "0 9 * * MON" */
    @Column(nullable = false)
    private String cronExpression;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private Instant lastRunAt;
    private Instant nextRunAt;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public enum Status { ACTIVE, PAUSED, CANCELLED }
}

