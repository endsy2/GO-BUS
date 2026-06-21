package com.busapp.userservice.dto.request;

import com.busapp.userservice.model.User;
import com.busapp.userservice.model.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserWalletRequest {
    private User user;

    private Currency currency;

    private Double amount;
}
