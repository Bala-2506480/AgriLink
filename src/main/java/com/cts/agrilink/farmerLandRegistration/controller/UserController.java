package com.cts.agrilink.farmerLandRegistration.controller;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.UserRequestDTO;
import com.cts.agrilink.farmerLandRegistration.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/farmerLandRegistration")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/createUser")
    public ResponseEntity<ApiResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
        userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO("User created successfully"));
    }

    @PutMapping("/updateUser/{userId}")
    public ResponseEntity<ApiResponseDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody UserRequestDTO dto) {
        userService.updateUser(userId, dto);
        return ResponseEntity.ok(new ApiResponseDTO("User updated successfully"));
    }

    // Hard Delete
    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity<ApiResponseDTO> deleteUser(@PathVariable Long userId) {
        ApiResponseDTO response = userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    // Soft Delete
    @DeleteMapping("/deleteUser/{userId}/soft")
    public ResponseEntity<ApiResponseDTO> softDeleteUser(@PathVariable Long userId) {
        ApiResponseDTO response = userService.softDeleteUser(userId);
        return ResponseEntity.ok(response);
    }
}
