package com.summershop.payment.kafka;

import com.summershop.kafka.config.KafkaTopics;
import com.summershop.kafka.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentEvent(PaymentEvent paymentEvent) {
        log.info("Sending payment event: {}", paymentEvent);
        kafkaTemplate.send(KafkaTopics.PAYMENT_EVENTS, paymentEvent.getPaymentId(), paymentEvent);
    }
}