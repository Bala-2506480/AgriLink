package com.cts.agrilink.auditLog.service;

import com.cts.agrilink.auditLog.dto.AuditLogDto;
import com.cts.agrilink.auditLog.model.AuditLog;
import com.cts.agrilink.auditLog.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogDto save(AuditLogDto dto) {
        // idempotency by eventId
        if (dto.getEventId() != null) {
            repository.findByEventId(dto.getEventId()).ifPresent(existing -> {
                // return existing by setting id and timestamp
                dto.setId(existing.getId());
                dto.setTimestamp(existing.getTimestamp());
            });
            if (dto.getId() != null) {
                return dto;
            }
        }

        AuditLog entity = AuditLog.builder()
                .eventId(dto.getEventId())
                .userId(dto.getUserId())
                .sessionId(dto.getSessionId())
                .action(dto.getAction())
                .module(dto.getModule())
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .beforeChange(dto.getBeforeChange())
                .afterChange(dto.getAfterChange())
                .changes(dto.getChanges())
                .ipAddress(dto.getIpAddress())
                .timestamp(dto.getTimestamp() == null ? Instant.now() : dto.getTimestamp())
                .build();

        AuditLog saved = repository.save(entity);
        dto.setId(saved.getId());
        dto.setTimestamp(saved.getTimestamp());
        return dto;
    }

    public Page<AuditLogDto> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDto);
    }

    public AuditLogDto findById(Long id) {
        return repository.findById(id).map(this::toDto).orElse(null);
    }

    public Page<AuditLogDto> findByUserId(Integer userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(this::toDto);
    }

    private AuditLogDto toDto(AuditLog a) {
        return AuditLogDto.builder()
                .id(a.getId())
                .eventId(a.getEventId())
                .userId(a.getUserId())
                .sessionId(a.getSessionId())
                .action(a.getAction())
                .module(a.getModule())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .beforeChange(a.getBeforeChange())
                .afterChange(a.getAfterChange())
                .changes(a.getChanges())
                .ipAddress(a.getIpAddress())
                .timestamp(a.getTimestamp())
                .build();
    }
}
