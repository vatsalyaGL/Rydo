package com.payments.service;

import com.payments.dto.PaymentRequest;
import com.payments.dto.PaymentResponseDTO;
import com.payments.entity.Payment;
import com.payments.enums.PaymentMethod;
import com.payments.enums.PaymentStatus;
import com.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // 🚀 CREATE PAYMENT (if not exists)
    public PaymentResponseDTO createPayment(PaymentRequest request) {
        if (request.getDriverId() == null) {
            throw new RuntimeException("Driver ID cannot be null");
        }

        Payment payment = paymentRepository.findByTripId(request.getTripId())
                .orElseGet(() -> saveNewPayment(request));

        // simulate async processing for frontend polling
        processPaymentAsync(payment.getTripId());

        return mapToDTO(payment);
    }

    // 🔹 Save a new Payment entity
    private Payment saveNewPayment(PaymentRequest request) {
        Payment payment = new Payment();

        payment.setId(UUID.randomUUID());
        payment.setTripId(request.getTripId());
        payment.setRiderId(request.getRiderId());
        payment.setDriverId(request.getDriverId());
        payment.setGrossAmount(request.getAmount());

        // 💰 Fee & tax calculation
        BigDecimal fee = request.getAmount().multiply(BigDecimal.valueOf(0.20));
        BigDecimal tax = request.getAmount().multiply(BigDecimal.valueOf(0.05));
        payment.setPlatformFee(fee);
        payment.setTaxAmount(tax);
        payment.setDriverPayout(request.getAmount().subtract(fee).subtract(tax));

        // 🔥 Initial status
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setIdempotencyKey(UUID.randomUUID().toString());

        // 📱 Generate UPI Link dynamically
        payment.setUpiLink(
                "upi://pay?pa=driver" + request.getDriverId()
                        + "@upi&pn=Driver&am=" + request.getAmount() + "&cu=INR"
        );

        return paymentRepository.save(payment);
    }

    // 🔍 GET PAYMENT STATUS (for polling)
    public PaymentResponseDTO getPayment(UUID tripId) {
        Payment payment = paymentRepository.findByTripId(tripId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return mapToDTO(payment);
    }

    // ✅ MANUAL VERIFY PAYMENT (optional)
    public PaymentResponseDTO verifyPayment(UUID tripId) {
        Payment payment = paymentRepository.findByTripId(tripId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return mapToDTO(payment);
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(OffsetDateTime.now());
        paymentRepository.save(payment);

        return mapToDTO(payment);
    }

    // 🔹 SIMULATED ASYNC PROCESSING (for frontend polling)
    public void processPaymentAsync(UUID tripId) {
        Payment payment = paymentRepository.findByTripId(tripId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);

            new Thread(() -> {
                try {
                    Thread.sleep(5000); // simulate 5-second payment processing
                    payment.setStatus(PaymentStatus.SUCCEEDED);
                    payment.setPaidAt(OffsetDateTime.now());
                    paymentRepository.save(payment);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // 🔹 Map Payment entity to PaymentResponseDTO for frontend
    private PaymentResponseDTO mapToDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(payment.getId());
        dto.setTripId(payment.getTripId());
        dto.setRiderId(payment.getRiderId());
        dto.setDriverId(payment.getDriverId());
        dto.setAmount(payment.getGrossAmount());
        dto.setStatus(payment.getStatus());
        dto.setUpiLink(payment.getUpiLink());
        return dto;
    }
}