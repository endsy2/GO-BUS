package com.busapp.bookingservice.dto.response;

import com.busapp.bookingservice.dto.KhqrStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KhqrResponse {
    private KhqrStatus status;
    private KhqrResponseData data;
}
