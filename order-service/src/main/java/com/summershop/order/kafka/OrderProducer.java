package com.summershop.order.kafka;

import com.summershop.kafka.config.KafkaTopics;
import com.summershop.kafka.events.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderEvent(OrderEvent orderEvent) {
        log.info("Sending order event: {}", orderEvent);
        kafkaTemplate.send(KafkaTopics.ORDER_EVENTS, orderEvent.getOrderId(), orderEvent);
    }
}