package com.cts.agrilink.identityAccess.service;

import com.cts.agrilink.identityAccess.exception.ResourceNotFoundException;
import com.cts.agrilink.identityAccess.dto.CreateRoleRequestDto;
import com.cts.agrilink.identityAccess.dto.RoleResponseDto;
import com.cts.agrilink.identityAccess.dto.UpdateRoleRequestDto;
import com.cts.agrilink.identityAccess.model.UserRole;
import com.cts.agrilink.identityAccess.repository.UserDetailsRepository;
import com.cts.agrilink.identityAccess.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final UserRoleRepository    userRoleRepository;
    private final UserDetailsRepository userDetailsRepository;

    // ── Create Role (Admin only) ───────────────────────────────────────────────
    @Transactional
    public RoleResponseDto createRole(CreateRoleRequestDto dto) {

        if (userRoleRepository.findByRoleName(dto.getRoleName()).isPresent()) {
            throw new IllegalStateException("Role already exists: " + dto.getRoleName());
        }

        UserRole role = UserRole.builder()
                .roleName(dto.getRoleName())
                .description(dto.getDescription())
                .status(UserRole.Status.A)
                .build();

        userRoleRepository.save(role);

        return toResponseDto(role);
    }

    // ── List all roles ──────────────────────────────────────────────────────────
    public List<RoleResponseDto> getAllRoles() {
        return userRoleRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    // ── Get role by id ────────────────────────────────────────────────────────────
    public RoleResponseDto getRole(Integer id) {
        return toResponseDto(findOrThrow(id));
    }

    // ── Update role (Admin only) ────────────────────────────────────────────────
    @Transactional
    public RoleResponseDto updateRole(Integer id, UpdateRoleRequestDto dto) {

        UserRole role = findOrThrow(id);

        if (dto.getRoleName() != null && !dto.getRoleName().isBlank()) {
            String newName = dto.getRoleName();
            userRoleRepository.findByRoleName(newName)
                    .filter(r -> !r.getRoleId().equals(id))
                    .ifPresent(r -> { throw new IllegalStateException("Role already exists: " + newName); });
            role.setRoleName(newName);
        }

        if (dto.getDescription() != null) {
            role.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null) {
            role.setStatus(UserRole.Status.valueOf(dto.getStatus()));
        }

        userRoleRepository.save(role);

        return toResponseDto(role);
    }

    // ── Delete role (Admin only) — hard delete, blocked if users are assigned ────
    @Transactional
    public void deleteRole(Integer id) {
        UserRole role = findOrThrow(id);
        if (userDetailsRepository.existsByRole_RoleId(id)) {
            throw new IllegalStateException("Role is assigned to one or more users and cannot be deleted");
        }
        userRoleRepository.delete(role);
    }

    private UserRole findOrThrow(Integer id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    private RoleResponseDto toResponseDto(UserRole role) {
        return RoleResponseDto.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .status(role.getStatus().name())
                .build();
    }
}
