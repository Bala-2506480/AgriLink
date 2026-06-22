package com.cts.agrilink.auditLog.repository;

import com.cts.agrilink.auditLog.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Optional<AuditLog> findByEventId(String eventId);
    Page<AuditLog> findByUserId(Integer userId, Pageable pageable);
}
