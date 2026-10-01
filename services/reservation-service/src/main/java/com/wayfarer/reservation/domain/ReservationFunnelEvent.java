package com.wayfarer.reservation.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record ReservationFunnelEvent(UUID sessionId, UUID reservationAttemptId,
                                    ReservationFunnelEventType eventType, ReservationFunnelScreen screen,
                                    String hotelId, String roomTypeId, Instant occurredAt) {
    public ReservationFunnelEvent {
        Objects.requireNonNull(sessionId);
        Objects.requireNonNull(reservationAttemptId);
        Objects.requireNonNull(eventType);
        Objects.requireNonNull(screen);
        Objects.requireNonNull(occurredAt);
    }
}
