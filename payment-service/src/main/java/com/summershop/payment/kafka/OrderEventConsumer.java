package com.summershop.payment.kafka;

import com.summershop.kafka.config.KafkaTopics;
import com.summershop.kafka.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = KafkaTopics.ORDER_EVENTS, groupId = "payment-service-group")
    public void handleOrderEvent(OrderEvent orderEvent) {
        log.info("Received order event: {}", orderEvent);

        // Process order events if needed
        switch (orderEvent.getEventType()) {
            case "CANCELLED":
                log.info("Order cancelled: {}, may need to process refund", orderEvent.getOrderId());
                break;
            default:
                log.debug("Order event type: {} for order: {}", orderEvent.getEventType(), orderEvent.getOrderId());
        }
    }
}