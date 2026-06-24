package com.cts.agrilink.identityAccess.controller;

import com.cts.agrilink.identityAccess.dto.CreateUserRequestDto;
import com.cts.agrilink.identityAccess.dto.ResetPasswordRequestDto;
import com.cts.agrilink.identityAccess.dto.UpdateUserRequestDto;
import com.cts.agrilink.identityAccess.dto.UserResponseDto;
import com.cts.agrilink.identityAccess.model.UserDetails;
import com.cts.agrilink.identityAccess.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agriLink/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // AgriLinkAdmin can create any user; ExtensionOfficer can create Farmers only (enforced in service)
    @PreAuthorize("hasRole('AgriLinkAdmin') or hasRole('ExtensionOfficer')")
    @PostMapping("/createUser")
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody CreateUserRequestDto dto,
                                                          @AuthenticationPrincipal UserDetails currentUser) {
        userService.createUser(dto, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User created successfully"));
    }

    @PreAuthorize("hasRole('AgriLinkAdmin')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('AgriLinkAdmin')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PreAuthorize("hasRole('AgriLinkAdmin')")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequestDto dto) {
        userService.updateUser(id, dto);
        return ResponseEntity.ok(Map.of("message", "User updated successfully"));
    }

    @PreAuthorize("hasRole('AgriLinkAdmin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
    }

    @PreAuthorize("hasRole('AgriLinkAdmin')")
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @PathVariable Integer id,
            @Valid @RequestBody ResetPasswordRequestDto dto) {
        userService.resetPassword(id, dto);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
