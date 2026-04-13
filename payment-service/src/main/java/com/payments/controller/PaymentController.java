package com.payments.controller;

import com.payments.dto.PaymentRequest;
import com.payments.dto.PaymentResponseDTO;
import com.payments.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 🚀 Create Payment
    @PostMapping
    public PaymentResponseDTO createPayment(@RequestBody PaymentRequest request) {
        PaymentResponseDTO payment = paymentService.createPayment(request);

        // simulate async processing for frontend polling
        paymentService.processPaymentAsync(payment.getTripId());

        return payment;
    }

    // 🔍 Get Payment Status (for polling)
    @GetMapping("/{tripId}")
    public PaymentResponseDTO getPayment(@PathVariable UUID tripId) {
        return paymentService.getPayment(tripId);
    }

    // ✅ Manual verify payment (optional admin use)
    @PostMapping("/{tripId}/verify")
    public PaymentResponseDTO verifyPayment(@PathVariable UUID tripId) {
        return paymentService.verifyPayment(tripId);
    }
}