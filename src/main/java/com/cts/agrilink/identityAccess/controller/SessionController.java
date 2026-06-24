package com.cts.agrilink.identityAccess.controller;

import com.cts.agrilink.identityAccess.dto.ChangePasswordRequestDto;
import com.cts.agrilink.identityAccess.dto.LoginRequestDto;
import com.cts.agrilink.identityAccess.dto.LoginResponseDto;
import com.cts.agrilink.identityAccess.dto.RefreshTokenRequestDto;
import com.cts.agrilink.identityAccess.model.UserDetails;
import com.cts.agrilink.identityAccess.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/agriLink/session")
@RequiredArgsConstructor
public class SessionController {

    private final UserService userService;

    // Public — permitted in SecurityConfig
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto,
                                                   HttpServletRequest request) {
        return ResponseEntity.ok(userService.login(dto, request));
    }

    // Public — permitted in SecurityConfig
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto dto) {
        return ResponseEntity.ok(userService.refreshToken(dto.getRefreshToken()));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal UserDetails currentUser) {
        userService.logout(currentUser.getUserId());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequestDto dto,
            @AuthenticationPrincipal UserDetails currentUser) {
        userService.changePassword(currentUser.getUserId(), dto);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
