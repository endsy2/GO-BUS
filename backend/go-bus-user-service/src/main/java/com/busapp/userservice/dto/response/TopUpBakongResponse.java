package com.busapp.userservice.dto.response;

import com.busapp.userservice.model.enums.Currency;
import com.busapp.userservice.model.enums.TopUpStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpBakongResponse {
    private int responseCode;
    private String responseMessage;
    private Long errorCode;
    private Object data;

}
