package com.cts.agrilink.identityAccess.dto;

import jakarta.validation.constraints.*;
import lombok.*;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
 
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;
 
    @NotBlank(message = "Password is required")
    private String password;
}
