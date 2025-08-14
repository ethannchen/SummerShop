package com.summershop.order.service;

import com.summershop.kafka.events.OrderEvent;
import com.summershop.order.dto.OrderRequest;
import com.summershop.order.dto.OrderResponse;
import com.summershop.order.entity.Order;
import com.summershop.order.kafka.OrderProducer;
import com.summershop.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;

    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // Calculate total amount
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order items
        List<Order.OrderItem> orderItems = request.getItems().stream()
                .map(item -> Order.OrderItem.builder()
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        // Create order entity
        Order order = Order.builder()
                .orderId(UUID.randomUUID())
                .customerId(request.getCustomerId())
                .orderStatus("PENDING")
                .paymentStatus("PENDING")
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Set items as JSON
        order.setItemsAsJson(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Get items for event
        List<Order.OrderItem> savedItems = savedOrder.getItemsFromJson();

        // Send order created event
        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(savedOrder.getOrderId().toString())
                .eventType("CREATED")
                .orderStatus(savedOrder.getOrderStatus())
                .customerId(savedOrder.getCustomerId())
                .totalAmount(savedOrder.getTotalAmount())
                .items(savedItems.stream()
                        .map(item -> OrderEvent.OrderItem.builder()
                                .itemId(item.getItemId())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .timestamp(LocalDateTime.now())
                .build();

        orderProducer.sendOrderEvent(orderEvent);

        return mapToResponse(savedOrder);
    }

    public OrderResponse cancelOrder(String orderId) {
        log.info("Cancelling order: {}", orderId);

        UUID orderUuid = UUID.fromString(orderId);
        Order order = orderRepository.findById(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if ("CANCELLED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Order already cancelled");
        }

        order.setOrderStatus("CANCELLED");
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        // Send order cancelled event
        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(updatedOrder.getOrderId().toString())
                .eventType("CANCELLED")
                .orderStatus(updatedOrder.getOrderStatus())
                .customerId(updatedOrder.getCustomerId())
                .totalAmount(updatedOrder.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .build();

        orderProducer.sendOrderEvent(orderEvent);

        return mapToResponse(updatedOrder);
    }

    public OrderResponse updateOrder(String orderId, OrderRequest request) {
        log.info("Updating order: {}", orderId);

        UUID orderUuid = UUID.fromString(orderId);
        Order order = orderRepository.findById(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Update fields
        order.setShippingAddress(request.getShippingAddress());
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        // Send order updated event
        OrderEvent orderEvent = OrderEvent.builder()
                .orderId(updatedOrder.getOrderId().toString())
                .eventType("UPDATED")
                .orderStatus(updatedOrder.getOrderStatus())
                .customerId(updatedOrder.getCustomerId())
                .totalAmount(updatedOrder.getTotalAmount())
                .timestamp(LocalDateTime.now())
                .build();

        orderProducer.sendOrderEvent(orderEvent);

        return mapToResponse(updatedOrder);
    }

    public OrderResponse getOrder(String orderId) {
        log.info("Fetching order: {}", orderId);

        UUID orderUuid = UUID.fromString(orderId);
        Order order = orderRepository.findById(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        return mapToResponse(order);
    }

    public void updatePaymentStatus(String orderId, String paymentStatus, String paymentId) {
        log.info("Updating payment status for order: {} to {}", orderId, paymentStatus);

        UUID orderUuid = UUID.fromString(orderId);
        Order order = orderRepository.findById(orderUuid)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setPaymentStatus(paymentStatus);
        order.setPaymentId(paymentId);

        if ("COMPLETED".equals(paymentStatus)) {
            order.setOrderStatus("CONFIRMED");
        } else if ("FAILED".equals(paymentStatus)) {
            order.setOrderStatus("PAYMENT_FAILED");
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getOrderId().toString())
                .customerId(order.getCustomerId())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentId(order.getPaymentId())
                .totalAmount(order.getTotalAmount())
                .items(order.getItemsFromJson()) // Parse JSON to get items
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}