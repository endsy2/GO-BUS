package com.busapp.bookingservice.dto.response;

import lombok.Data;

@Data
public class BakongQrResponse {
    private String qr;
    private String md5;
}
