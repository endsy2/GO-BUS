package com.busapp.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BakongResponse {
    private int responseCode;
    private String responseMessage;
    private Long errorCode;
    private Object data;
}
