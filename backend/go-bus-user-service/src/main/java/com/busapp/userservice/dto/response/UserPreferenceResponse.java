package com.busapp.userservice.dto.response;

import com.busapp.userservice.model.enums.Theme;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceResponse {
    private Long  id;
    private Long  userId;
    private Theme theme;
}
