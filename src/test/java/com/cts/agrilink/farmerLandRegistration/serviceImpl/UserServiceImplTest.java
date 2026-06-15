package com.cts.agrilink.farmerLandRegistration.serviceImpl;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.UserRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.FarmerProfile;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.repository.FarmerProfileRepository;
import com.cts.agrilink.farmerLandRegistration.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock FarmerProfileRepository farmerProfileRepository;
    @InjectMocks UserServiceImpl userService;

    private User mockUser;
    private UserRequestDTO mockDTO;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().userId(1L).name("Ravi").email("ravi@example.com")
                .phone("9876543210").passwordHash("hash123")
                .status(User.UserStatus.Active).build();

        mockDTO = new UserRequestDTO();
        mockDTO.setName("Ravi"); mockDTO.setEmail("ravi@example.com");
        mockDTO.setPhone("9876543210"); mockDTO.setPasswordHash("hash123");
        mockDTO.setStatus("Active");
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail("ravi@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(mockUser);
        assertNotNull(userService.createUser(mockDTO));
    }

    @Test
    void createUser_DuplicateEmail_Throws409() {
        when(userRepository.existsByEmail("ravi@example.com")).thenReturn(true);
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> userService.createUser(mockDTO)).getStatusCode().value());
    }

    @Test
    void updateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenReturn(mockUser);
        mockDTO.setPhone("9000000001"); mockDTO.setStatus("Inactive");
        assertNotNull(userService.updateUser(1L, mockDTO));
    }

    @Test
    void updateUser_NotFound_Throws404() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(99L, mockDTO)).getStatusCode().value());
    }

    @Test
    void updateUser_InvalidStatus_Throws400() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        mockDTO.setStatus("INVALID");
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> userService.updateUser(1L, mockDTO)).getStatusCode().value());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(farmerProfileRepository.findByUser_UserId(1L)).thenReturn(Collections.emptyList());
        ApiResponseDTO result = userService.deleteUser(1L);
        assertEquals("User deleted successfully", result.getMessage());
        verify(userRepository).delete(mockUser);
    }

    @Test
    void deleteUser_LinkedRecords_Throws409() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(farmerProfileRepository.findByUser_UserId(1L)).thenReturn(List.of(new FarmerProfile()));
        assertEquals(409, assertThrows(ResponseStatusException.class,
                () -> userService.deleteUser(1L)).getStatusCode().value());
    }

    @Test
    void softDeleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any())).thenReturn(mockUser);
        ApiResponseDTO result = userService.softDeleteUser(1L);
        assertEquals("User deactivated successfully", result.getMessage());
        assertEquals(User.UserStatus.Inactive, mockUser.getStatus());
    }
}
