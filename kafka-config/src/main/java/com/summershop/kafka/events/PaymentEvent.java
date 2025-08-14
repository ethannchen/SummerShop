package com.summershop.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent implements Serializable {
    private String paymentId;
    private String eventType; // INITIATED, COMPLETED, FAILED, REFUNDED
    private String orderId;
    private BigDecimal amount;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime timestamp;
    private String failureReason;
}