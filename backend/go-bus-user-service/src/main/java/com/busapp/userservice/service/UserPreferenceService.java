package com.busapp.userservice.service;

import com.busapp.userservice.dto.request.UserPreferenceRequest;
import com.busapp.userservice.dto.response.UserPreferenceResponse;

public interface UserPreferenceService {

    UserPreferenceResponse getByUserId(Long userId);

    UserPreferenceResponse upsertPreference(Long userId, UserPreferenceRequest request);
}
