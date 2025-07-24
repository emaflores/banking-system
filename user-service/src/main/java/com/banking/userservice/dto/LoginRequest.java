package com.banking.userservice.dto;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class LoginRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Size(max = 255, message = "Email is too long")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}
