package com.origin.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alarm_items")
@Data
@NoArgsConstructor
public class AlarmItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private Instant triggerAt;

    private boolean dismissed = false;

    @CreationTimestamp
    private Instant createdAt;
}

