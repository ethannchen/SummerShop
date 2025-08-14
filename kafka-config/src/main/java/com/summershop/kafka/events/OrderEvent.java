package com.summershop.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent implements Serializable {
    private String orderId;
    private String eventType; // CREATED, UPDATED, CANCELLED
    private String orderStatus;
    private Long customerId;
    private BigDecimal totalAmount;
    private List<OrderItem> items;
    private LocalDateTime timestamp;
    private String paymentId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        private String itemId;
        private Integer quantity;
        private BigDecimal price;
    }
}