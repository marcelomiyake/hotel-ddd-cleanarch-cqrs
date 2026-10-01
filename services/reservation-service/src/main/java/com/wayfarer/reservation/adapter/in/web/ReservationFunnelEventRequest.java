package com.wayfarer.reservation.adapter.in.web;

import com.wayfarer.reservation.domain.ReservationFunnelEventType;
import com.wayfarer.reservation.domain.ReservationFunnelScreen;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReservationFunnelEventRequest(
        @NotNull UUID sessionId,
        @NotNull UUID reservationAttemptId,
        @NotNull ReservationFunnelEventType eventType,
        @NotNull ReservationFunnelScreen screen,
        @Size(max = 80) String hotelId,
        @Size(max = 100) String roomTypeId) {
}
