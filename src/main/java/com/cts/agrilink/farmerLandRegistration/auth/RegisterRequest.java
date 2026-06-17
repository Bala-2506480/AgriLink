package com.cts.agrilink.farmerLandRegistration.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is mandatory")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Size(max = 150)
    private String email;

    @Size(max = 15)
    private String phone;

    @NotBlank(message = "Password is mandatory")
    private String password;

    // "ADMIN" or "FARMER" — defaults to FARMER
    private String role;
}