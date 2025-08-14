package com.summershop.order.kafka;

import com.summershop.kafka.config.KafkaTopics;
import com.summershop.kafka.events.PaymentEvent;
import com.summershop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS, groupId = "order-service-group")
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        log.info("Received payment event: {}", paymentEvent);

        switch (paymentEvent.getEventType()) {
            case "COMPLETED":
                orderService.updatePaymentStatus(paymentEvent.getOrderId(), "COMPLETED", paymentEvent.getPaymentId());
                break;
            case "FAILED":
                orderService.updatePaymentStatus(paymentEvent.getOrderId(), "FAILED", paymentEvent.getPaymentId());
                break;
            case "REFUNDED":
                orderService.updatePaymentStatus(paymentEvent.getOrderId(), "REFUNDED", paymentEvent.getPaymentId());
                break;
            default:
                log.warn("Unknown payment event type: {}", paymentEvent.getEventType());
        }
    }
}