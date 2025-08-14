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

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_EVENTS,
            groupId = "order-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        try {
            log.info("Received payment event: Type={}, OrderId={}, PaymentId={}, Status={}",
                    paymentEvent.getEventType(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getPaymentId(),
                    paymentEvent.getPaymentStatus());

            if (paymentEvent.getOrderId() == null) {
                log.warn("Received payment event with null orderId, skipping");
                return;
            }

            switch (paymentEvent.getEventType()) {
                case "COMPLETED":
                    orderService.updatePaymentStatus(
                            paymentEvent.getOrderId(),
                            "COMPLETED",
                            paymentEvent.getPaymentId()
                    );
                    log.info("Successfully updated order {} with payment status COMPLETED",
                            paymentEvent.getOrderId());
                    break;

                case "FAILED":
                    orderService.updatePaymentStatus(
                            paymentEvent.getOrderId(),
                            "FAILED",
                            paymentEvent.getPaymentId()
                    );
                    log.info("Successfully updated order {} with payment status FAILED",
                            paymentEvent.getOrderId());
                    break;

                case "REFUNDED":
                    orderService.updatePaymentStatus(
                            paymentEvent.getOrderId(),
                            "REFUNDED",
                            paymentEvent.getPaymentId()
                    );
                    log.info("Successfully updated order {} with payment status REFUNDED",
                            paymentEvent.getOrderId());
                    break;

                default:
                    log.warn("Unknown payment event type: {}", paymentEvent.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing payment event for order: {}",
                    paymentEvent.getOrderId(), e);
            // Consider implementing retry logic or dead letter queue here
        }
    }
}