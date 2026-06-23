package com.cts.agrilink.auditLog.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", unique = true)
    private String eventId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "session_id")
    private Integer sessionId;

    private String action;

    private String module;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Lob
    @Column(name = "before_change")
    private String beforeChange; // Changed from 'before'

    @Lob
    @Column(name = "after_change")
    private String afterChange;  // Changed from 'after'

    @Lob
    private String changes;

    @Column(name = "ip_address")
    private String ipAddress;

    private Instant timestamp;
}
