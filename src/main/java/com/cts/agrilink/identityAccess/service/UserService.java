package com.cts.agrilink.identityAccess.service;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cts.agrilink.identityAccess.dto.ChangePasswordRequestDto;
import com.cts.agrilink.identityAccess.dto.CreateUserRequestDto;
import com.cts.agrilink.identityAccess.dto.LoginRequestDto;
import com.cts.agrilink.identityAccess.dto.LoginResponseDto;
import com.cts.agrilink.identityAccess.dto.ResetPasswordRequestDto;
import com.cts.agrilink.identityAccess.dto.UpdateUserRequestDto;
import com.cts.agrilink.identityAccess.dto.UserResponseDto;
import com.cts.agrilink.identityAccess.exception.ForbiddenException;
import com.cts.agrilink.identityAccess.exception.ResourceNotFoundException;
import com.cts.agrilink.identityAccess.model.UserDetails;
import com.cts.agrilink.identityAccess.model.UserRole;
import com.cts.agrilink.identityAccess.model.UserSession;
import com.cts.agrilink.identityAccess.repository.UserDetailsRepository;
import com.cts.agrilink.identityAccess.repository.UserRoleRepository;
import com.cts.agrilink.identityAccess.repository.UserSessionRepository;
import com.cts.agrilink.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDetailsRepository userDetailsRepository;
    private final UserRoleRepository    userRoleRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;

    @Value("${jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    private static final String ROLE_EXTENSION_OFFICER = "ExtensionOfficer";
    private static final String ROLE_FARMER            = "Farmer";

    // Create User
    // AgriLinkAdmin may create any user; ExtensionOfficer may create Farmers only.
    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto dto, UserDetails currentUser) {

        // Check duplicate email
        if (userDetailsRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email already registered");
        }

        // Resolve role
        UserRole role = userRoleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.getRoleId()));

        if (role.getStatus() == UserRole.Status.I) {
            throw new IllegalStateException("Cannot assign an inactive role");
        }

        // An ExtensionOfficer can only create Farmer accounts
        String callerRole = currentUser.getRole().getRoleName();
        if (ROLE_EXTENSION_OFFICER.equals(callerRole) && !ROLE_FARMER.equals(role.getRoleName())) {
            throw new ForbiddenException("ExtensionOfficer can only create Farmer accounts");
        }

        // Build and save user
        UserDetails user = UserDetails.builder()
                .role(role)
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .regionId(dto.getRegionId())
                .status(UserDetails.Status.A)
                .build();

        userDetailsRepository.save(user);

        return toResponseDto(user);
    }

    //List all users (Admin only)
    public List<UserResponseDto> getAllUsers() {
        return userDetailsRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    // Get user by id (Admin only)
    public UserResponseDto getUser(Integer id) {
        return toResponseDto(findOrThrow(id));
    }

    // Update user (Admin only)
    @Transactional
    public UserResponseDto updateUser(Integer id, UpdateUserRequestDto dto) {

        UserDetails user = findOrThrow(id);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getRegionId() != null) {
            user.setRegionId(dto.getRegionId());
        }
        if (dto.getRoleId() != null) {
            UserRole role = userRoleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.getRoleId()));
            if (role.getStatus() == UserRole.Status.I) {
                throw new IllegalStateException("Cannot assign an inactive role");
            }
            user.setRole(role);
        }
        if (dto.getStatus() != null) {
            user.setStatus(UserDetails.Status.valueOf(dto.getStatus()));
        }

        userDetailsRepository.save(user);

        return toResponseDto(user);
    }

    // Soft-delete (deactivate) a user (Admin only)
    @Transactional
    public void deleteUser(Integer id) {
        UserDetails user = findOrThrow(id);
        user.setStatus(UserDetails.Status.I);
        userDetailsRepository.save(user);
        // Revoke active sessions so the deactivated user can't keep using existing tokens
        userSessionRepository.revokeAllActiveSessionsByUserId(id);
    }

    private UserDetails findOrThrow(Integer id) {
        return userDetailsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    //Login
    @Transactional
    public LoginResponseDto login(LoginRequestDto dto, HttpServletRequest request) {

        // Find user by email
        UserDetails user = userDetailsRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check account status
        if (user.getStatus() == UserDetails.Status.S) {
            throw new IllegalArgumentException("Account is suspended. Contact your administrator.");
        }
        if (user.getStatus() == UserDetails.Status.I) {
            throw new IllegalArgumentException("Account is inactive. Contact your administrator.");
        }

        // Generate JWT access token
        String accessToken = jwtUtil.generateAccessToken(user);

        // Generate opaque refresh token and store only its SHA-256 hash
        String rawRefreshToken = generateSecureToken();
        String refreshTokenHash = sha256Hex(rawRefreshToken);

        // Persist session
        UserSession session = UserSession.builder()
                .user(user)
                .refreshTokenHash(refreshTokenHash)
                .refreshTokenExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .ipAddress(request.getRemoteAddr())
                .deviceInfo(request.getHeader("User-Agent"))
                .status(UserSession.Status.Active)
                .build();

        userSessionRepository.save(session);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)        // raw token sent to client ONCE
                .expiresIn(accessTokenExpiryMs / 1000)
                .userId(user.getUserId())
                .roleName(user.getRole().getRoleName())
                .regionId(user.getRegionId())
                .build();
    }

    // Refresh access token (with rotation)
    @Transactional
    public LoginResponseDto refreshToken(String rawRefreshToken) {

        // Look up the session by the hash of the presented refresh token
        UserSession session = userSessionRepository.findByRefreshTokenHash(sha256Hex(rawRefreshToken))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        // Must still be active
        if (session.getStatus() != UserSession.Status.Active) {
            throw new IllegalArgumentException("Session is no longer active. Please log in again.");
        }

        // Must not be expired
        if (session.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(UserSession.Status.Expired);
            userSessionRepository.save(session);
            throw new IllegalArgumentException("Refresh token expired. Please log in again.");
        }

        UserDetails user = session.getUser();

        // Issue a fresh access token
        String accessToken = jwtUtil.generateAccessToken(user);

        // Rotate the refresh token — old one is invalidated, new one persisted
        String newRawRefreshToken = generateSecureToken();
        session.setRefreshTokenHash(sha256Hex(newRawRefreshToken));
        session.setRefreshTokenRotatedAt(LocalDateTime.now());
        userSessionRepository.save(session);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(newRawRefreshToken)     // new raw token returned to client
                .expiresIn(accessTokenExpiryMs / 1000)
                .userId(user.getUserId())
                .roleName(user.getRole().getRoleName())
                .regionId(user.getRegionId())
                .build();
    }

    // Logout
    @Transactional
    public void logout(Integer userId) {
        // Revoke all active sessions for this user
        userSessionRepository.revokeAllActiveSessionsByUserId(userId);
    }

    //  Change own password (authenticated, self-service) 
    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequestDto dto) {
        UserDetails user = findOrThrow(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalStateException("New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userDetailsRepository.save(user);

        // Force re-login everywhere after a password change
        userSessionRepository.revokeAllActiveSessionsByUserId(userId);
    }

    // Reset a user's password (Admin only) 
    @Transactional
    public void resetPassword(Integer userId, ResetPasswordRequestDto dto) {
        UserDetails user = findOrThrow(userId);
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userDetailsRepository.save(user);

        // Revoke the user's sessions so the old password's tokens stop working
        userSessionRepository.revokeAllActiveSessionsByUserId(userId);
    }

    //Helpers 
    private UserResponseDto toResponseDto(UserDetails user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleName(user.getRole().getRoleName())
                .regionId(user.getRegionId())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // Generates a cryptographically secure random token
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // SHA-256 hex hash
    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}
