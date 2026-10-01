package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.ReservationFunnelEvent;

import java.time.Clock;
import java.time.Instant;

public class RecordReservationFunnelEventHandler {
    private final ReservationFunnelEventStore eventStore;
    private final Clock clock;

    public RecordReservationFunnelEventHandler(ReservationFunnelEventStore eventStore, Clock clock) {
        this.eventStore = eventStore;
        this.clock = clock;
    }

    public void handle(RecordReservationFunnelEventCommand command) {
        eventStore.append(new ReservationFunnelEvent(command.sessionId(), command.reservationAttemptId(),
                command.eventType(), command.screen(), command.hotelId(), command.roomTypeId(), Instant.now(clock)));
    }
}
