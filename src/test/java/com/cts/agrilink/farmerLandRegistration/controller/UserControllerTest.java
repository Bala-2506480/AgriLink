package com.cts.agrilink.farmerLandRegistration.controller;

import com.cts.agrilink.farmerLandRegistration.dto.ApiResponseDTO;
import com.cts.agrilink.farmerLandRegistration.dto.UserRequestDTO;
import com.cts.agrilink.farmerLandRegistration.model.User;
import com.cts.agrilink.farmerLandRegistration.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean UserService userService;
    @Autowired ObjectMapper objectMapper;

    private User mockUser;
    private UserRequestDTO mockDTO;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().userId(1L).name("Ravi").email("ravi@example.com")
                .phone("9876543210").passwordHash("hash123").status(User.UserStatus.Active).build();

        mockDTO = new UserRequestDTO();
        mockDTO.setName("Ravi"); mockDTO.setEmail("ravi@example.com");
        mockDTO.setPhone("9876543210"); mockDTO.setPasswordHash("hash123"); mockDTO.setStatus("Active");
    }

    @Test
    void createUser_Returns201() throws Exception {
        when(userService.createUser(any())).thenReturn(mockUser);
        mockMvc.perform(post("/farmerLandRegistration/createUser")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }

    @Test
    void createUser_MissingName_Returns400() throws Exception {
        mockDTO.setName("");
        mockMvc.perform(post("/farmerLandRegistration/createUser")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_DuplicateEmail_Returns409() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered"));
        mockMvc.perform(post("/farmerLandRegistration/createUser")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_Returns200() throws Exception {
        when(userService.updateUser(eq(1L), any())).thenReturn(mockUser);
        mockMvc.perform(put("/farmerLandRegistration/updateUser/1")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    void updateUser_NotFound_Returns404() throws Exception {
        when(userService.updateUser(eq(99L), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        mockMvc.perform(put("/farmerLandRegistration/updateUser/99")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(mockDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_Returns204() throws Exception {
        when(userService.deleteUser(1L)).thenReturn(new ApiResponseDTO("User deleted successfully"));
        mockMvc.perform(delete("/farmerLandRegistration/deleteUser/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_LinkedRecords_Returns409() throws Exception {
        when(userService.deleteUser(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "User cannot be deleted, linked records exist"));
        mockMvc.perform(delete("/farmerLandRegistration/deleteUser/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void softDeleteUser_Returns200() throws Exception {
        when(userService.softDeleteUser(1L)).thenReturn(new ApiResponseDTO("User deactivated successfully"));
        mockMvc.perform(delete("/farmerLandRegistration/deleteUser/1/soft"))
                .andExpect(status().isOk());
    }
}
