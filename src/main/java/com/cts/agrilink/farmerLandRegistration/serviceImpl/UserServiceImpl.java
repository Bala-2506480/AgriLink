package com.cts.agrilink.farmerLandRegistration.serviceImpl;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.UserRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.Role;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.service.UserService;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .passwordHash(passwordEncoder.encode(dto.getPasswordHash()))
                .role(dto.getRole() != null ? Role.valueOf(dto.getRole()) : Role.FARMER)
                .status(dto.getStatus() != null
                        ? User.UserStatus.valueOf(dto.getStatus())
                        : User.UserStatus.Active)
                .build();

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, UserRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (dto.getName() != null)   user.setName(dto.getName());
        if (dto.getPhone() != null)  user.setPhone(dto.getPhone());
        if (dto.getStatus() != null) {
            try {
                user.setStatus(User.UserStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
            }
        }

        return userRepository.save(user);
    }

    @Override
    public ApiResponseDTO deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!farmerProfileRepository.findByUser_UserId(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User cannot be deleted, linked records exist");
        }

        userRepository.delete(user);
        return new ApiResponseDTO("User deleted successfully");
    }

    @Override
    public ApiResponseDTO softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setStatus(User.UserStatus.Inactive);
        userRepository.save(user);
        return new ApiResponseDTO("User deactivated successfully");
    }
}