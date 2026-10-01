package com.wayfarer.reservation;

import com.wayfarer.reservation.application.RecordReservationFunnelEventCommand;
import com.wayfarer.reservation.application.RecordReservationFunnelEventHandler;
import com.wayfarer.reservation.domain.ReservationFunnelEvent;
import com.wayfarer.reservation.domain.ReservationFunnelEventType;
import com.wayfarer.reservation.domain.ReservationFunnelScreen;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationFunnelEventTest {
    private static final Instant OCCURRED_AT = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    void recordsAnEventWithTheApplicationClockAndOptionalHotelContext() {
        AtomicReference<ReservationFunnelEvent> saved = new AtomicReference<>();
        RecordReservationFunnelEventHandler handler = new RecordReservationFunnelEventHandler(
                saved::set, Clock.fixed(OCCURRED_AT, ZoneOffset.UTC));
        UUID sessionId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();

        handler.handle(new RecordReservationFunnelEventCommand(sessionId, attemptId,
                ReservationFunnelEventType.SCREEN_VIEWED, ReservationFunnelScreen.HOME, null, null));

        assertThat(saved.get()).isEqualTo(new ReservationFunnelEvent(sessionId, attemptId,
                ReservationFunnelEventType.SCREEN_VIEWED, ReservationFunnelScreen.HOME, null, null, OCCURRED_AT));
    }
}
