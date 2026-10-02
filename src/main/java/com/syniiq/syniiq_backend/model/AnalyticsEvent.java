package com.syniiq.syniiq_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "analytics_events", indexes = {
        @Index(name = "idx_analytics_created_at", columnList = "created_at"),
        @Index(name = "idx_analytics_event_type", columnList = "event_type")
})
@Getter
@Setter
@NoArgsConstructor
public class AnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_path", nullable = false, length = 255)
    private String pagePath;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "user_session_id", nullable = false, length = 100)
    private String userSessionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}