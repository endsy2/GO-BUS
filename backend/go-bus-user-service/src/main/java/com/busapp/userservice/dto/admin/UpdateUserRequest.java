package com.busapp.userservice.dto.admin;

import com.busapp.userservice.model.enums.Gender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified request for updating user information (admin endpoint).
 * Excludes: password, email, googleId, isEmployee (managed via separate endpoints).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, numbers, and underscores")
    private String userName;

    private String fullName;

    private String phone;

    private String image;

    private Gender gender;
}
