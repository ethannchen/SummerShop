package com.summershop.order.dto;

import com.summershop.order.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private String orderId;
    private Long customerId;
    private String orderStatus;
    private String paymentStatus;
    private String paymentId;
    private BigDecimal totalAmount;
    private List<Order.OrderItem> items;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}