package com.busapp.userservice.controller;

import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.request.UpdateProfileRequest;
import com.busapp.userservice.dto.request.UserRequest;
import com.busapp.userservice.dto.response.UserBasicResponse;
import com.busapp.userservice.dto.response.UserResponseDetail;
import com.busapp.userservice.service.UserService;
import com.busapp.userservice.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final UserUtil userUtil;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponseDetail>> getCurrentUserProfile(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        ApiResponse<UserResponseDetail> response = ApiResponse.of(
                "Profile retrieved successfully",
                userService.getUserById(userId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/specification")
    public ResponseEntity<ApiResponse<Page<UserResponseDetail>>> getUserSpecification(
            @RequestParam(value = "pageStart", defaultValue = "1", required = false) int pageStart,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "googleId", required = false) String googleId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "isActive",required = false) Boolean isActive,
            @RequestParam(value = "isEmployee", required = false) Boolean isEmployee,
            @RequestParam(value = "isDeleted", required = false, defaultValue = "false") Boolean isDeleted
    ) {
        ApiResponse<Page<UserResponseDetail>> response = ApiResponse.of(
                "User retrieved successfully",
                userService.filterBySpec(pageStart - 1, pageSize, userId, email, phone, googleId, username, isEmployee, isDeleted,isActive)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDetail>> getUserById(@PathVariable Long id) {
        ApiResponse<UserResponseDetail> response = ApiResponse.of(
                "User retrieved successfully",
                userService.getUserById(id));
        return ResponseEntity.ok(response);
    }

    /**
     * Get basic user info by ID (lightweight - only essential fields)
     * Use this endpoint when you don't need full user details
     */
    @GetMapping("/{id}/basic")
    public ResponseEntity<ApiResponse<UserBasicResponse>> getUserBasicById(@PathVariable Long id) {
        ApiResponse<UserBasicResponse> response = ApiResponse.of(
                "User basic info retrieved successfully",
                userService.getUserBasicById(id));
        return ResponseEntity.ok(response);
    }
    @PostMapping("/batch/basic")
    public ResponseEntity<ApiResponse<List<UserBasicResponse>>> getUserBasicByIds(@RequestBody Set<Long> ids) {
        ApiResponse<List<UserBasicResponse>> response = ApiResponse.of(
                "User basic info retrieved successfully",
                userService.getUserBasicByIds(ids));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<UserResponseDetail>>> getUsersByIds(@RequestBody Set<Long> userIds) {
        ApiResponse<List<UserResponseDetail>> response = ApiResponse.of(
                "Users retrieved successfully",
                userService.getUsersByIds(userIds));
        return ResponseEntity.ok(response);
    }

    /**
     * Lightweight lookup by exact username — used by other services (Feign) to
     * resolve a username to a user id. Returns 200 with null data when not found
     * so the caller can treat it as an empty result rather than a 404.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserBasicResponse>> getUserByUsername(@PathVariable String username) {
        ApiResponse<UserBasicResponse> response = ApiResponse.of(
                "User retrieved successfully",
                userService.getUserByUsername(username));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponseDetail>> getUserByEmail(@PathVariable String email) {
        ApiResponse<UserResponseDetail> response = ApiResponse.of(
                "User retrieved successfully",
                userService.getUserByEmail(email));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<UserResponseDetail>> getUserByPhone(@PathVariable String phone) {
        ApiResponse<UserResponseDetail> response = ApiResponse.of(
                "User retrieved successfully",
                userService.getUserByPhone(phone));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/google/{googleId}")
    public ResponseEntity<ApiResponse<UserResponseDetail>> getUserByGoogleId(@PathVariable String googleId) {
        ApiResponse<UserResponseDetail> response = ApiResponse.of(
                "User retrieved successfully",
                userService.getUserByGoogleId(googleId));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDetail>> createUser(
            @Valid @RequestBody UserRequest userRequest) {
        ApiResponse<UserResponseDetail> response = ApiResponse.of(
                "User created successfully",
                userService.createUser(userRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/current-user")
    public ResponseEntity<ApiResponse<UserResponseDetail>> updateCurrentUser(
            @Valid @RequestBody UpdateProfileRequest request) {
        ApiResponse<UserResponseDetail> response = ApiResponse.of(
                "User updated successfully",
                userService.updateProfile(userUtil.getCurrentUserId(), request));
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDetail>> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @PathVariable Long userId
    ){
        ApiResponse<UserResponseDetail>response=ApiResponse.of(
                "User updated successfully",
                userService.updateProfile(userId, request));
        return ResponseEntity.ok(response);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<Void> response = ApiResponse.of(HttpStatus.OK.value(), "User deleted successfully", null);
        return ResponseEntity.ok(response);
    }
}
