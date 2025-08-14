package com.summershop.payment.kafka;

import com.summershop.kafka.config.KafkaTopics;
import com.summershop.kafka.events.OrderEvent;
import com.summershop.kafka.events.PaymentEvent;
import com.summershop.payment.entity.Payment;
import com.summershop.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "payment-service-group")
    @Transactional
    public void handleOrderEvent(OrderEvent orderEvent) {
        log.info("Received order event: {}", orderEvent);

        // Process order events based on type
        switch (orderEvent.getEventType()) {
            case "CANCELLED":
                processOrderCancellation(orderEvent);
                break;
            case "CREATED":
                log.info("Order created: {}, payment will be initiated separately", orderEvent.getOrderId());
                break;
            case "UPDATED":
                log.info("Order updated: {}", orderEvent.getOrderId());
                break;
            default:
                log.debug("Order event type: {} for order: {}", orderEvent.getEventType(), orderEvent.getOrderId());
        }
    }

    private void processOrderCancellation(OrderEvent orderEvent) {
        log.info("Processing order cancellation for order: {}", orderEvent.getOrderId());

        try {
            // Find payment by order ID
            Payment payment = paymentRepository.findByOrderId(orderEvent.getOrderId())
                    .orElse(null);

            if (payment == null) {
                log.warn("No payment found for cancelled order: {}", orderEvent.getOrderId());
                return;
            }

            // Check if payment can be refunded
            if (!"COMPLETED".equals(payment.getStatus())) {
                log.info("Payment not completed for order: {}, current status: {}",
                        orderEvent.getOrderId(), payment.getStatus());

                // If payment is still processing, mark it as cancelled
                if ("PROCESSING".equals(payment.getStatus()) || "PENDING".equals(payment.getStatus())) {
                    payment.setStatus("CANCELLED");
                    payment.setUpdatedAt(LocalDateTime.now());
                    paymentRepository.save(payment);

                    // Send payment cancelled event
                    PaymentEvent paymentEvent = PaymentEvent.builder()
                            .paymentId(payment.getId())
                            .eventType("CANCELLED")
                            .orderId(payment.getOrderId())
                            .amount(payment.getAmount())
                            .paymentStatus("CANCELLED")
                            .paymentMethod(payment.getPaymentMethod())
                            .timestamp(LocalDateTime.now())
                            .build();

                    paymentProducer.sendPaymentEvent(paymentEvent);
                }
                return;
            }

            // Check if already refunded
            if ("REFUNDED".equals(payment.getStatus())) {
                log.info("Payment already refunded for order: {}", orderEvent.getOrderId());
                return;
            }

            // Process automatic refund
            processAutomaticRefund(payment, orderEvent);

        } catch (Exception e) {
            log.error("Error processing order cancellation for order: {}", orderEvent.getOrderId(), e);
        }
    }

    private void processAutomaticRefund(Payment payment, OrderEvent orderEvent) {
        log.info("Processing automatic refund for payment: {} due to order cancellation", payment.getId());

        // Calculate refund amount (full refund for cancelled orders)
        BigDecimal refundAmount = payment.getAmount();

        // Update payment status to REFUNDED
        payment.setStatus("REFUNDED");
        payment.setRefundedAmount(refundAmount);
        payment.setUpdatedAt(LocalDateTime.now());

        Payment updatedPayment = paymentRepository.save(payment);

        // Send refund event
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .paymentId(updatedPayment.getId())
                .eventType("REFUNDED")
                .orderId(updatedPayment.getOrderId())
                .amount(refundAmount)
                .paymentStatus("REFUNDED")
                .paymentMethod(updatedPayment.getPaymentMethod())
                .timestamp(LocalDateTime.now())
                .failureReason("Order cancelled - automatic refund")
                .build();

        paymentProducer.sendPaymentEvent(paymentEvent);

        log.info("Automatic refund processed successfully for payment: {}, amount: {}",
                payment.getId(), refundAmount);
    }
}