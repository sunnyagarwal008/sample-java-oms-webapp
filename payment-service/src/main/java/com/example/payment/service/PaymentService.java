package com.example.payment.service;

import com.example.payment.dto.CreatePaymentRequest;
import com.example.payment.model.Payment;
import com.example.payment.model.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setCustomerEmail(request.getCustomerEmail());
        payment.setTransactionReference(UUID.randomUUID().toString());
        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
    }

    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public List<Payment> getPaymentsByCustomerEmail(String email) {
        return paymentRepository.findByCustomerEmail(email);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional
    public Payment completePayment(Long id) {
        Payment payment = getPaymentById(id);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException("Payment is not in PENDING state");
        }
        payment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment failPayment(Long id) {
        Payment payment = getPaymentById(id);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException("Payment is not in PENDING state");
        }
        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long id) {
        Payment payment = getPaymentById(id);
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException("Only completed payments can be refunded");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    public static class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidPaymentStateException extends RuntimeException {
        public InvalidPaymentStateException(String message) {
            super(message);
        }
    }
}
