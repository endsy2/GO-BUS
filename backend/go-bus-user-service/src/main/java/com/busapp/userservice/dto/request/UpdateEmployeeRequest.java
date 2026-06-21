package com.busapp.userservice.dto.request;

import com.busapp.userservice.model.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEmployeeRequest {

    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, numbers, and underscores")
    private String userName;

    private String fullName;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;

    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{9,}$",
            message = "Password must be at least 9 characters long, include uppercase, lowercase, number, and special character"
    )
    private String password;

    private String image;

    private Gender gender;

    private Long roleId;
}
