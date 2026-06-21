package com.busapp.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight user response with only essential fields.
 * Use this for listings, references, or when full user details are not needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicResponse {
    private Long id;
    private String userName;
    private String fullName;
    private String email;
    private String phone;
}
