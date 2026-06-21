package com.busapp.userservice.dto.response;


import java.math.BigDecimal;

@lombok.Data
public class Data {
    private BigDecimal amount;
    private String method;
    private String transaction_id;
    private String status;
    private String paidAt;
}
