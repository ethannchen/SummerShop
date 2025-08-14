package com.summershop.payment.service;

import com.summershop.kafka.events.PaymentEvent;
import com.summershop.payment.dto.PaymentRequest;
import com.summershop.payment.dto.PaymentResponse;
import com.summershop.payment.dto.RefundRequest;
import com.summershop.payment.entity.Payment;
import com.summershop.payment.kafka.PaymentProducer;
import com.summershop.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    @Transactional
    public PaymentResponse submitPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Check idempotency
        Payment existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .orElse(null);

        if (existingPayment != null) {
            log.info("Payment already exists with idempotency key: {}", request.getIdempotencyKey());
            return mapToResponse(existingPayment);
        }

        // Create payment
        Payment payment = Payment.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status("PROCESSING")
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Send payment initiated event
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .paymentId(savedPayment.getId())
                .eventType("INITIATED")
                .orderId(savedPayment.getOrderId())
                .amount(savedPayment.getAmount())
                .paymentStatus(savedPayment.getStatus())
                .paymentMethod(savedPayment.getPaymentMethod())
                .timestamp(LocalDateTime.now())
                .build();

        paymentProducer.sendPaymentEvent(paymentEvent);

        // Simulate payment processing
        processPayment(savedPayment);

        return mapToResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse updatePayment(String paymentId, PaymentRequest request) {
        log.info("Updating payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if ("COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Cannot update completed payment");
        }

        payment.setPaymentMethod(request.getPaymentMethod());
        Payment updatedPayment = paymentRepository.save(payment);

        return mapToResponse(updatedPayment);
    }

    @Transactional
    public PaymentResponse refundPayment(String paymentId, RefundRequest request) {
        log.info("Processing refund for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Can only refund completed payments");
        }

        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : payment.getAmount();

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new RuntimeException("Refund amount exceeds payment amount");
        }

        payment.setStatus("REFUNDED");
        payment.setRefundedAmount(refundAmount);
        Payment updatedPayment = paymentRepository.save(payment);

        // Send refund event
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .paymentId(updatedPayment.getId())
                .eventType("REFUNDED")
                .orderId(updatedPayment.getOrderId())
                .amount(refundAmount)
                .paymentStatus(updatedPayment.getStatus())
                .paymentMethod(updatedPayment.getPaymentMethod())
                .timestamp(LocalDateTime.now())
                .build();

        paymentProducer.sendPaymentEvent(paymentEvent);

        return mapToResponse(updatedPayment);
    }

    public PaymentResponse getPayment(String paymentId) {
        log.info("Fetching payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        return mapToResponse(payment);
    }

    private void processPayment(Payment payment) {
        // Simulate payment processing
        try {
            Thread.sleep(1000); // Simulate processing delay

            // Simulate 90% success rate
            if (Math.random() < 0.9) {
                payment.setStatus("COMPLETED");
                payment.setTransactionId(UUID.randomUUID().toString());

                // Send payment completed event
                PaymentEvent paymentEvent = PaymentEvent.builder()
                        .paymentId(payment.getId())
                        .eventType("COMPLETED")
                        .orderId(payment.getOrderId())
                        .amount(payment.getAmount())
                        .paymentStatus(payment.getStatus())
                        .paymentMethod(payment.getPaymentMethod())
                        .timestamp(LocalDateTime.now())
                        .build();

                paymentProducer.sendPaymentEvent(paymentEvent);
            } else {
                payment.setStatus("FAILED");
                payment.setFailureReason("Payment declined by bank");

                // Send payment failed event
                PaymentEvent paymentEvent = PaymentEvent.builder()
                        .paymentId(payment.getId())
                        .eventType("FAILED")
                        .orderId(payment.getOrderId())
                        .amount(payment.getAmount())
                        .paymentStatus(payment.getStatus())
                        .paymentMethod(payment.getPaymentMethod())
                        .failureReason(payment.getFailureReason())
                        .timestamp(LocalDateTime.now())
                        .build();

                paymentProducer.sendPaymentEvent(paymentEvent);
            }

            paymentRepository.save(payment);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted", e);
        }
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}