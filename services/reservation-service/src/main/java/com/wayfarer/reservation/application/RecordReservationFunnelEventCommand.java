package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.ReservationFunnelEventType;
import com.wayfarer.reservation.domain.ReservationFunnelScreen;

import java.util.UUID;

public record RecordReservationFunnelEventCommand(UUID sessionId, UUID reservationAttemptId,
                                                  ReservationFunnelEventType eventType,
                                                  ReservationFunnelScreen screen,
                                                  String hotelId, String roomTypeId) {
}
