package com.busapp.userservice.controller;

import com.busapp.userservice.dto.response.ApiResponse;
import com.busapp.userservice.dto.request.UserPreferenceRequest;
import com.busapp.userservice.dto.response.UserPreferenceResponse;
import com.busapp.userservice.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.of(
                "Preferences retrieved successfully",
                preferenceService.getByUserId(userId)));
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> upsertPreference(
            @PathVariable Long userId,
            @Valid @RequestBody UserPreferenceRequest request) {
        return ResponseEntity.ok(ApiResponse.of(
                "Preferences saved successfully",
                preferenceService.upsertPreference(userId, request)));
    }
}
