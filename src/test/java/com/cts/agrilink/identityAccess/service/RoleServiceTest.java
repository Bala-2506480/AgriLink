package com.cts.agrilink.identityAccess.service;

import com.cts.agrilink.exception.ResourceNotFoundException;
import com.cts.agrilink.identityAccess.dto.CreateRoleRequestDto;
import com.cts.agrilink.identityAccess.dto.RoleResponseDto;
import com.cts.agrilink.identityAccess.dto.UpdateRoleRequestDto;
import com.cts.agrilink.identityAccess.model.UserRole;
import com.cts.agrilink.identityAccess.repository.UserDetailsRepository;
import com.cts.agrilink.identityAccess.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private UserRoleRepository    userRoleRepository;
    @Mock private UserDetailsRepository userDetailsRepository;

    @InjectMocks private RoleService roleService;

    private UserRole role(int id, String name, UserRole.Status status) {
        return UserRole.builder().roleId(id).roleName(name).description("desc").status(status).build();
    }

    private CreateRoleRequestDto createDto(String name) {
        CreateRoleRequestDto dto = new CreateRoleRequestDto();
        dto.setRoleName(name);
        dto.setDescription("a role");
        return dto;
    }

    // ════════════════════════════════ createRole ════════════════════════════
    @Test
    void createRole_success() {
        when(userRoleRepository.findByRoleName("Auditor")).thenReturn(Optional.empty());

        RoleResponseDto res = roleService.createRole(createDto("Auditor"));

        assertEquals("Auditor", res.getRoleName());
        assertEquals("ACTIVE", res.getStatus());
        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    @DisplayName("createRole: duplicate role name -> IllegalStateException")
    void createRole_duplicate() {
        when(userRoleRepository.findByRoleName("Farmer"))
                .thenReturn(Optional.of(role(6, "Farmer", UserRole.Status.ACTIVE)));

        assertThrows(IllegalStateException.class, () -> roleService.createRole(createDto("Farmer")));
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void createRole_createdActiveByDefault() {
        when(userRoleRepository.findByRoleName(any())).thenReturn(Optional.empty());

        ArgumentCaptor<UserRole> cap = ArgumentCaptor.forClass(UserRole.class);
        roleService.createRole(createDto("Auditor"));
        verify(userRoleRepository).save(cap.capture());
        assertEquals(UserRole.Status.ACTIVE, cap.getValue().getStatus());
    }

    // ════════════════════════════════ read ═══════════════════════════════════
    @Test
    void getAllRoles() {
        when(userRoleRepository.findAll()).thenReturn(List.of(
                role(1, "AgriLinkAdmin", UserRole.Status.ACTIVE),
                role(6, "Farmer", UserRole.Status.ACTIVE)));

        List<RoleResponseDto> res = roleService.getAllRoles();

        assertEquals(2, res.size());
        assertEquals("AgriLinkAdmin", res.getFirst().getRoleName());
    }

    @Test
    void getAllRoles_empty() {
        when(userRoleRepository.findAll()).thenReturn(List.of());
        assertTrue(roleService.getAllRoles().isEmpty());
    }

    @Test
    void getRole_success() {
        when(userRoleRepository.findById(6)).thenReturn(Optional.of(role(6, "Farmer", UserRole.Status.ACTIVE)));

        RoleResponseDto res = roleService.getRole(6);

        assertEquals(6, res.getRoleId());
        assertEquals("Farmer", res.getRoleName());
    }

    @Test
    void getRole_notFound() {
        when(userRoleRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.getRole(99));
    }

    // ════════════════════════════════ updateRole ════════════════════════════
    @Test
    void updateRole_name_success() {
        UserRole r = role(7, "Old", UserRole.Status.ACTIVE);
        when(userRoleRepository.findById(7)).thenReturn(Optional.of(r));
        when(userRoleRepository.findByRoleName("New")).thenReturn(Optional.empty());
        UpdateRoleRequestDto d = new UpdateRoleRequestDto(); d.setRoleName("New");

        roleService.updateRole(7, d);

        assertEquals("New", r.getRoleName());
        verify(userRoleRepository).save(r);
    }

    @Test
    @DisplayName("updateRole: rename collision with another role -> IllegalStateException")
    void updateRole_duplicateName() {
        UserRole r = role(7, "Old", UserRole.Status.ACTIVE);
        when(userRoleRepository.findById(7)).thenReturn(Optional.of(r));
        when(userRoleRepository.findByRoleName("Farmer"))
                .thenReturn(Optional.of(role(6, "Farmer", UserRole.Status.ACTIVE)));
        UpdateRoleRequestDto d = new UpdateRoleRequestDto(); d.setRoleName("Farmer");

        assertThrows(IllegalStateException.class, () -> roleService.updateRole(7, d));
    }

    @Test
    @DisplayName("updateRole: renaming to its own current name is allowed")
    void updateRole_sameNameNoConflict() {
        UserRole r = role(7, "Coordinator", UserRole.Status.ACTIVE);
        when(userRoleRepository.findById(7)).thenReturn(Optional.of(r));
        when(userRoleRepository.findByRoleName("Coordinator")).thenReturn(Optional.of(r));
        UpdateRoleRequestDto d = new UpdateRoleRequestDto(); d.setRoleName("Coordinator");

        assertDoesNotThrow(() -> roleService.updateRole(7, d));
        verify(userRoleRepository).save(r);
    }

    @Test
    void updateRole_description() {
        UserRole r = role(7, "Coordinator", UserRole.Status.ACTIVE);
        when(userRoleRepository.findById(7)).thenReturn(Optional.of(r));
        UpdateRoleRequestDto d = new UpdateRoleRequestDto(); d.setDescription("updated");

        roleService.updateRole(7, d);
        assertEquals("updated", r.getDescription());
    }

    @Test
    void updateRole_statusToInactive() {
        UserRole r = role(7, "Coordinator", UserRole.Status.ACTIVE);
        when(userRoleRepository.findById(7)).thenReturn(Optional.of(r));
        UpdateRoleRequestDto d = new UpdateRoleRequestDto(); d.setStatus("INACTIVE");

        roleService.updateRole(7, d);
        assertEquals(UserRole.Status.INACTIVE, r.getStatus());
    }

    @Test
    void updateRole_notFound() {
        when(userRoleRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> roleService.updateRole(99, new UpdateRoleRequestDto()));
    }

    // ════════════════════════════════ deleteRole ════════════════════════════
    @Test
    void deleteRole_success_whenNoUsersAssigned() {
        UserRole r = role(7, "Coordinator", UserRole.Status.ACTIVE);
        when(userRoleRepository.findById(7)).thenReturn(Optional.of(r));
        when(userDetailsRepository.existsByRole_RoleId(7)).thenReturn(false);

        roleService.deleteRole(7);

        verify(userRoleRepository).delete(r);
    }

    @Test
    @DisplayName("deleteRole: role with assigned users -> IllegalStateException, not deleted")
    void deleteRole_blockedWhenUsersAssigned() {
        UserRole r = role(6, "Farmer", UserRole.Status.ACTIVE);
        when(userRoleRepository.findById(6)).thenReturn(Optional.of(r));
        when(userDetailsRepository.existsByRole_RoleId(6)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> roleService.deleteRole(6));
        verify(userRoleRepository, never()).delete(any());
    }

    @Test
    void deleteRole_notFound() {
        when(userRoleRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> roleService.deleteRole(99));
        verify(userDetailsRepository, never()).existsByRole_RoleId(any());
    }
}
