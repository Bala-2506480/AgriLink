package com.cts.agrilink.farmerLandRegistration.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void builder_SetsAllFields() {
        User user = User.builder()
                .userId(1L).name("Ravi Kumar").email("ravi@example.com")
                .phone("9876543210").passwordHash("hash123")
                .status(User.UserStatus.Active).role(Role.FARMER).build();

        assertEquals(1L, user.getUserId());
        assertEquals("Ravi Kumar", user.getName());
        assertEquals("ravi@example.com", user.getEmail());
        assertEquals(User.UserStatus.Active, user.getStatus());
        assertEquals(Role.FARMER, user.getRole());
    }

    @Test
    void setter_UpdatesStatus() {
        User user = new User();
        user.setStatus(User.UserStatus.Inactive);
        assertEquals(User.UserStatus.Inactive, user.getStatus());
    }

    @Test
    void enum_HasExactlyTwoValues() {
        assertEquals(2, User.UserStatus.values().length);
    }

    @Test
    void enum_ValueOf_ValidAndInvalid() {
        assertEquals(User.UserStatus.Active, User.UserStatus.valueOf("Active"));
        assertThrows(IllegalArgumentException.class, () -> User.UserStatus.valueOf("INVALID"));
    }

    @Test
    void noArgsConstructor_CreatesEmptyUser() {
        User user = new User();
        assertNull(user.getUserId());
        assertNull(user.getName());
    }

    @Test
    void role_SetAndGet() {
        User user = new User();
        user.setRole(Role.ADMIN);
        assertEquals(Role.ADMIN, user.getRole());
    }
}