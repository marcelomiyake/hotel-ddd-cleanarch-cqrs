package com.wayfarer.reservation.adapter.out.postgres;

import com.wayfarer.reservation.application.ReservationQueries;
import com.wayfarer.reservation.application.ReservationStore;
import com.wayfarer.reservation.domain.GuestDetails;
import com.wayfarer.reservation.domain.Reservation;
import com.wayfarer.reservation.domain.ReservationStatus;
import com.wayfarer.reservation.domain.StayPeriod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcReservationStore implements ReservationStore, ReservationQueries {
    private static final String COLUMNS = "id, idempotency_key, hotel_id, room_type_id, room_name, guest_name, "
            + "guest_email, check_in, check_out, guest_count, room_count, status, price_per_night_cents, "
            + "total_price_cents, currency, created_at";
    private static final String SELECT_FROM_RESERVATIONS = "SELECT " + COLUMNS + " FROM reservations";
    private static final RowMapper<Reservation> RESERVATION_MAPPER = (row, index) -> new Reservation(
            row.getObject("id", UUID.class), row.getString("idempotency_key"), row.getString("hotel_id"),
            row.getString("room_type_id"), row.getString("room_name"),
            new GuestDetails(row.getString("guest_name"), row.getString("guest_email")),
            new StayPeriod(row.getObject("check_in", LocalDate.class), row.getObject("check_out", LocalDate.class)),
            row.getInt("guest_count"), row.getInt("room_count"),
            ReservationStatus.valueOf(row.getString("status")), row.getLong("price_per_night_cents"),
            row.getLong("total_price_cents"), row.getString("currency").trim(),
            row.getTimestamp("created_at").toInstant());

    private final JdbcTemplate jdbc;

    public JdbcReservationStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void lockIdempotencyKey(String key) {
        jdbc.queryForList("SELECT pg_advisory_xact_lock(hashtext(?))", Object.class, key);
    }

    @Override
    public Optional<Reservation> findByIdempotencyKey(String key) {
        return jdbc.query(SELECT_FROM_RESERVATIONS + " WHERE idempotency_key = ?",
                RESERVATION_MAPPER, key).stream().findFirst();
    }

    @Override
    public Reservation save(Reservation reservation) {
        int affected = jdbc.update("""
                INSERT INTO reservations
                    (id, idempotency_key, hotel_id, room_type_id, room_name, guest_name, guest_email,
                     check_in, check_out, guest_count, room_count, status, price_per_night_cents,
                     total_price_cents, currency, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status
                WHERE reservations.idempotency_key = EXCLUDED.idempotency_key
                """, reservation.id(), reservation.idempotencyKey(), reservation.hotelId(), reservation.roomTypeId(),
                reservation.roomName(), reservation.guest().fullName(), reservation.guest().email(),
                reservation.stay().checkIn(), reservation.stay().checkOut(),
                reservation.guests(), reservation.rooms(), reservation.status().name(),
                reservation.pricePerNightCents(), reservation.totalPriceCents(), reservation.currency(),
                Timestamp.from(reservation.createdAt()));
        if (affected != 1) {
            throw new IllegalStateException("Reservation id belongs to a different idempotency key.");
        }
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return jdbc.query(SELECT_FROM_RESERVATIONS + " WHERE id = ?", RESERVATION_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<Reservation> findByGuestEmail(String normalizedEmail) {
        return jdbc.query(SELECT_FROM_RESERVATIONS + " WHERE guest_email = ? ORDER BY created_at DESC",
                RESERVATION_MAPPER, normalizedEmail);
    }
}
