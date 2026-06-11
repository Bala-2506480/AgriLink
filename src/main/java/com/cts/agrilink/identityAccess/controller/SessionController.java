package com.cts.agrilink.identityAccess.controller;


import com.cts.agrilink.identityAccess.dto.LoginRequestDto;
import com.cts.agrilink.identityAccess.dto.LoginResponseDto;
import com.cts.agrilink.identityAccess.dto.RefreshTokenRequestDto;
import com.cts.agrilink.identityAccess.model.UserDetails;
import com.cts.agrilink.identityAccess.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/agriLink/session")
@RequiredArgsConstructor
public class SessionController {

    private final UserService userService;

    // POST /agriLink/session/login  — public
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto,
                                                   HttpServletRequest request) {
        return ResponseEntity.ok(userService.login(dto, request));
    }

    // POST /agriLink/session/refresh  — public (access token may be expired)
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto dto) {
        return ResponseEntity.ok(userService.refreshToken(dto.getRefreshToken()));
    }

    // POST /agriLink/session/logout  — requires valid JWT
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal UserDetails currentUser) {
        userService.logout(currentUser.getUserId());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}