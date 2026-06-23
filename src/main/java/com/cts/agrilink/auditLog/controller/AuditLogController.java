package com.cts.agrilink.auditLog.controller;

import com.cts.agrilink.auditLog.dto.AuditLogDto;
import com.cts.agrilink.auditLog.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agriLink/auditLog")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<Void> createAuditLog(@RequestBody AuditLogDto dto) {
        auditLogService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<Page<AuditLogDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(auditLogService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDto> getById(@PathVariable Long id) {
        AuditLogDto dto = auditLogService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogDto>> getByUser(@PathVariable Integer userId, Pageable pageable) {
        return ResponseEntity.ok(auditLogService.findByUserId(userId, pageable));
    }
}
