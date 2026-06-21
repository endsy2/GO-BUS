package com.busapp.userservice.dto.request;

import com.busapp.userservice.model.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, numbers, and underscores")
    private String userName;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String phone;

    /** Maps to passwordHash on the User entity. */
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$",
            message = "Password must be at least 9 characters long, include uppercase, lowercase, number, and special character"
    )
    private String password;

    /** Optional Google OAuth ID. */
    private String googleId;

    /** Optional profile image URL or base64. */
    private String image;

    private Gender gender;

    private Boolean isEmployee;

    private Long roleId;
}
