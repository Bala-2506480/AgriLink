package com.cts.agrilink.auditLog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDto {
    private Long id;
    private String eventId;
    private Integer userId;
    private Integer sessionId;
    private String action;
    private String module;
    private String entityType;
    private String entityId;
    private String beforeChange;
    private String afterChange;
    private String changes;
    private String ipAddress;
    private Instant timestamp;
}
