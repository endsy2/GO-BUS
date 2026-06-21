package com.busapp.bookingservice.dto.request;

import com.busapp.bookingservice.model.enums.Currency;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BakongRequest {

    @NotNull
    private Long bookingId;
    @Enumerated
    private Currency currency;
}
