package com.wayfarer.reservation.adapter.out.postgres;

import com.wayfarer.reservation.application.ReservationFunnelEventStore;
import com.wayfarer.reservation.domain.ReservationFunnelEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class JdbcReservationFunnelEventStore implements ReservationFunnelEventStore {
    private final JdbcTemplate jdbc;

    public JdbcReservationFunnelEventStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void append(ReservationFunnelEvent event) {
        jdbc.update("""
                INSERT INTO reservation_funnel_events
                    (session_id, reservation_attempt_id, event_type, screen, hotel_id, room_type_id, occurred_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, event.sessionId(), event.reservationAttemptId(), event.eventType().name(),
                event.screen().value(), event.hotelId(), event.roomTypeId(), Timestamp.from(event.occurredAt()));
    }
}
