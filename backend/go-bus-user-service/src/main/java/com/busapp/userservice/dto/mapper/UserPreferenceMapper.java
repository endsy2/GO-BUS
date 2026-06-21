package com.busapp.userservice.dto.mapper;

import com.busapp.userservice.dto.request.UserPreferenceRequest;
import com.busapp.userservice.dto.response.UserPreferenceResponse;
import com.busapp.userservice.model.User;
import com.busapp.userservice.model.UserPreference;
import org.springframework.stereotype.Component;

@Component
public class UserPreferenceMapper {

    public UserPreferenceResponse toResponse(UserPreference preference) {
        UserPreferenceResponse response = new UserPreferenceResponse();
        response.setId(preference.getId());
        response.setUserId(preference.getUser().getId());
        response.setTheme(preference.getTheme());
        return response;
    }

    public UserPreference toEntity(UserPreferenceRequest request, User user) {
        UserPreference preference = new UserPreference();
        preference.setUser(user);
        preference.setTheme(request.getTheme());
        return preference;
    }
}
