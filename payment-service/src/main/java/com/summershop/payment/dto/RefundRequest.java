package com.summershop.payment.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @Positive(message = "Refund amount must be positive")
    private BigDecimal amount;

    private String reason;
}