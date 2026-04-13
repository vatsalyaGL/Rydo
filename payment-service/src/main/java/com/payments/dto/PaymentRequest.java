package com.payments.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentRequest {

    private UUID tripId;
    private UUID riderId;
    private UUID driverId;
    private BigDecimal amount;

    // ✅ Getters and Setters
    public UUID getTripId() {
        return tripId;
    }

    public void setTripId(UUID tripId) {
        this.tripId = tripId;
    }

    public UUID getRiderId() {
        return riderId;
    }

    public void setRiderId(UUID riderId) {
        this.riderId = riderId;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}