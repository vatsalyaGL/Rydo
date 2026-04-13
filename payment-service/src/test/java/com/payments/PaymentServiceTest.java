package com.payments;

import com.payments.dto.PaymentRequest;
import com.payments.dto.PaymentResponseDTO;
import com.payments.entity.Payment;
import com.payments.enums.PaymentMethod;
import com.payments.enums.PaymentStatus;
import com.payments.repository.PaymentRepository;
import com.payments.service.PaymentService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private UUID tripId;
    private UUID riderId;
    private UUID driverId;
    private PaymentRequest request;
    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        tripId   = UUID.randomUUID();
        riderId  = UUID.randomUUID();
        driverId = UUID.randomUUID();

        request = new PaymentRequest();
        request.setTripId(tripId);
        request.setRiderId(riderId);
        request.setDriverId(driverId);
        request.setAmount(new BigDecimal("100.00"));

        pendingPayment = new Payment();
        pendingPayment.setId(UUID.randomUUID());
        pendingPayment.setTripId(tripId);
        pendingPayment.setRiderId(riderId);
        pendingPayment.setDriverId(driverId);
        pendingPayment.setGrossAmount(new BigDecimal("100.00"));
        pendingPayment.setPlatformFee(new BigDecimal("20.00"));
        pendingPayment.setTaxAmount(new BigDecimal("5.00"));
        pendingPayment.setDriverPayout(new BigDecimal("75.00"));
        pendingPayment.setStatus(PaymentStatus.PENDING);
        pendingPayment.setPaymentMethod(PaymentMethod.UPI);
        pendingPayment.setIdempotencyKey(UUID.randomUUID().toString());
        pendingPayment.setUpiLink("upi://pay?pa=driver" + driverId + "@upi&pn=Driver&am=100.00&cu=INR");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createPayment
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createPayment()")
    class CreatePayment {


        @Test
        @DisplayName("throws RuntimeException when driverId is null")
        void throwsWhenDriverIdNull() {
            request.setDriverId(null);

            assertThatThrownBy(() -> paymentService.createPayment(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Driver ID cannot be null");
        }

        @Test
        @DisplayName("generates UPI link with driver ID and amount")
        void generatesUpiLink() {

            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(pendingPayment));

            when(paymentRepository.save(any(Payment.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            PaymentResponseDTO result = paymentService.createPayment(request);

            assertThat(result.getUpiLink()).contains(driverId.toString());
            assertThat(result.getUpiLink()).contains("upi://pay");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getPayment
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getPayment()")
    class GetPayment {

        @Test
        @DisplayName("returns DTO for found payment")
        void returnsDTOWhenFound() {
            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.of(pendingPayment));

            PaymentResponseDTO result = paymentService.getPayment(tripId);

            assertThat(result.getTripId()).isEqualTo(tripId);
            assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("throws RuntimeException when payment not found")
        void throwsWhenNotFound() {
            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.getPayment(tripId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Payment not found");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // verifyPayment
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("verifyPayment()")
    class VerifyPayment {

        @Test
        @DisplayName("sets status to SUCCEEDED and returns DTO")
        void setsStatusSucceeded() {

            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.of(pendingPayment));

            when(paymentRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            PaymentResponseDTO result = paymentService.verifyPayment(tripId);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
            verify(paymentRepository).save(argThat(p ->
                    p.getStatus() == PaymentStatus.SUCCEEDED));
        }

        @Test
        @DisplayName("returns immediately without saving when already SUCCEEDED")
        void returnsImmediately_whenAlreadySucceeded() {
            pendingPayment.setStatus(PaymentStatus.SUCCEEDED);

            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.of(pendingPayment));

            PaymentResponseDTO result = paymentService.verifyPayment(tripId);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws RuntimeException when payment not found")
        void throwsWhenNotFound() {
            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.verifyPayment(tripId))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // processPaymentAsync
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("processPaymentAsync()")
    class ProcessPaymentAsync {

        @Test
        @DisplayName("sets payment to PROCESSING when PENDING")
        void setsProcessingStatus_whenPending() {

            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.of(pendingPayment));

            when(paymentRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            paymentService.processPaymentAsync(tripId);

            assertThat(pendingPayment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
            verify(paymentRepository, atLeastOnce()).save(pendingPayment);
        }

        @Test
        @DisplayName("does not update status when already PROCESSING")
        void doesNotUpdate_whenAlreadyProcessing() {
            pendingPayment.setStatus(PaymentStatus.PROCESSING);

            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.of(pendingPayment));

            paymentService.processPaymentAsync(tripId);

            verify(paymentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws RuntimeException when payment not found")
        void throwsWhenNotFound() {
            when(paymentRepository.findByTripId(tripId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.processPaymentAsync(tripId))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}