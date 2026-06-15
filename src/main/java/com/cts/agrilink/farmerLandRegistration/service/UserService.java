package com.cts.agrilink.farmerLandRegistration.service;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.UserRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.User;

public interface UserService {
    User createUser(UserRequestDTO dto);
    User updateUser(Long userId, UserRequestDTO dto);
    ApiResponseDTO deleteUser(Long userId);         // Hard Delete
    ApiResponseDTO softDeleteUser(Long userId);     // Soft Delete
}
