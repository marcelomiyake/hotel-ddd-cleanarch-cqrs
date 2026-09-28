package com.wayfarer.reservation;

import com.wayfarer.reservation.adapter.in.web.AvailabilityResponse;
import com.wayfarer.reservation.adapter.in.web.PlaceReservationRequest;
import com.wayfarer.reservation.adapter.in.web.ReservationCommandController;
import com.wayfarer.reservation.adapter.in.web.ReservationExceptionHandler;
import com.wayfarer.reservation.adapter.in.web.ReservationQueryController;
import com.wayfarer.reservation.adapter.in.web.ReservationResponse;
import com.wayfarer.reservation.application.ReservationQueries;
import com.wayfarer.reservation.application.ReservationStore;
import com.wayfarer.reservation.application.RoomInventory;
import com.wayfarer.reservation.domain.DomainRuleViolation;
import com.wayfarer.reservation.domain.IdempotencyConflictException;
import com.wayfarer.reservation.domain.Reservation;
import com.wayfarer.reservation.domain.ReservationAccessDeniedException;
import com.wayfarer.reservation.domain.ReservationNotFoundException;
import com.wayfarer.reservation.domain.ReservationStatus;
import com.wayfarer.reservation.domain.RoomUnavailableException;
import com.wayfarer.reservation.domain.StayPeriod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
class ReservationIntegrationTest {
    private static final String ROOM_ID = "room-casa-mar-terrace";
    private static final String HOTEL_ID = "rio-casa-do-mar";
    private static final LocalDate TODAY = LocalDate.now(ZoneOffset.UTC);

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl() + "&currentSchema=hotel_reservation");
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private ReservationCommandController commands;

    @Autowired
    private ReservationQueryController queries;

    @Autowired
    private ReservationExceptionHandler errors;

    @Autowired
    private ReservationStore reservations;

    @Autowired
    private ReservationQueries reservationQueries;

    @Autowired
    private RoomInventory inventory;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void placesAnIdempotentReservationAndServesReadModelsFromPostgres() {
        PlaceReservationRequest request = request(30, "ALEX@example.com");
        LocalDate checkIn = request.checkIn();
        StayPeriod stay = new StayPeriod(checkIn, request.checkOut());

        AvailabilityResponse before = queries.availability(checkIn, request.checkOut(), 2).stream()
                .filter(offer -> offer.roomTypeId().equals(ROOM_ID)).findFirst().orElseThrow();
        assertThat(before.availableRooms()).isEqualTo(7);
        assertThat(before.hotelId()).isEqualTo(HOTEL_ID);
        assertThat(before.currency()).isEqualTo("BRL");

        ReservationResponse created = commands.place("idem-one", request);
        ReservationResponse replayed = commands.place("idem-one", request);
        assertThat(created.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(created.guestEmail()).isEqualTo("alex@example.com");
        assertThat(created.totalPriceCents()).isEqualTo(296000);
        assertThat(replayed.id()).isEqualTo(created.id());
        assertThat(roomCount(ROOM_ID, checkIn)).isEqualTo(6);

        ReservationResponse detail = queries.details(created.id(), " ALEX@example.com ");
        assertThat(detail.roomName()).isEqualTo("Terrace King");
        assertThat(detail.nights()).isEqualTo(2);
        assertThat(queries.history("ALEX@example.com")).containsExactly(detail);

        assertThatThrownBy(() -> commands.place("idem-one", request(30, "alex@example.com", 1)))
                .isInstanceOf(IdempotencyConflictException.class);
        assertThat(roomCount(ROOM_ID, checkIn)).isEqualTo(6);
        assertThat(reservationQueries.findById(created.id())).isPresent();
        assertThat(reservationQueries.findByGuestEmail("alex@example.com")).hasSize(1);
        assertThat(stay.nights()).isEqualTo(2);
    }

    @Test
    void cancellationChecksGuestIdentityAndRestoresEveryNightOnce() {
        PlaceReservationRequest request = request(45, "guest@example.com");
        ReservationResponse created = commands.place("idem-cancel", request);
        LocalDate checkIn = request.checkIn();
        assertThat(roomCount(ROOM_ID, checkIn)).isEqualTo(6);

        assertThatThrownBy(() -> commands.cancel(created.id(), "someone-else@example.com"))
                .isInstanceOf(ReservationAccessDeniedException.class);
        assertThat(roomCount(ROOM_ID, checkIn)).isEqualTo(6);

        ReservationResponse cancelled = commands.cancel(created.id(), " GUEST@example.com ");
        assertThat(cancelled.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(roomCount(ROOM_ID, checkIn)).isEqualTo(7);
        assertThatThrownBy(() -> commands.cancel(created.id(), "guest@example.com"))
                .hasMessageContaining("Only a confirmed reservation can be cancelled");
        assertThat(roomCount(ROOM_ID, checkIn)).isEqualTo(7);
    }

    @Test
    void rejectsInvalidQueriesMissingRoomsAndUnavailableInventory() {
        assertThatThrownBy(() -> queries.availability(TODAY.minusDays(1), TODAY.plusDays(1), 2))
                .isInstanceOf(DomainRuleViolation.class).hasMessageContaining("past");
        assertThatThrownBy(() -> queries.availability(TODAY.plusDays(10), TODAY.plusDays(11), 13))
                .isInstanceOf(DomainRuleViolation.class).hasMessageContaining("between 1 and 12");
        assertThatThrownBy(() -> queries.history(" ")).isInstanceOf(DomainRuleViolation.class);
        assertThatThrownBy(() -> queries.details(UUID.randomUUID(), "guest@example.com"))
                .isInstanceOf(ReservationNotFoundException.class);

        PlaceReservationRequest request = request(60, "soldout@example.com");
        StayPeriod stay = new StayPeriod(request.checkIn(), request.checkOut());
        jdbc.update("UPDATE inventory_days SET available_rooms = 0 WHERE room_type_id = ? AND inventory_date = ?",
                ROOM_ID, java.sql.Date.valueOf(request.checkIn()));
        assertThatThrownBy(() -> commands.place("idem-soldout", request)).isInstanceOf(RoomUnavailableException.class);
        assertThatThrownBy(() -> inventory.reserve(ROOM_ID, stay, 1)).isInstanceOf(RoomUnavailableException.class);
        assertThat(reservationQueries.findByGuestEmail("soldout@example.com")).isEmpty();
    }

    @Test
    void databaseStoreRejectsAnIdentifierOwnedByAnotherRequestKey() {
        ReservationResponse response = commands.place("idem-original", request(75, "owner@example.com"));
        Reservation saved = reservationQueries.findById(response.id()).orElseThrow();
        Reservation conflicting = new Reservation(saved.id(), "different-key", saved.hotelId(), saved.roomTypeId(),
                saved.roomName(), saved.guest(), saved.stay(), saved.guests(), saved.rooms(), saved.status(),
                saved.pricePerNightCents(), saved.totalPriceCents(), saved.currency(), saved.createdAt());

        assertThatThrownBy(() -> reservations.save(conflicting))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("different idempotency key");
    }

    @Test
    void mapsDomainErrorsToProblemDetails() {
        assertThat(errors.conflict(new RoomUnavailableException(ROOM_ID)).getStatus())
                .isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(errors.invalidRequest(new DomainRuleViolation("bad request")).getStatus())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(errors.notFound(new ReservationNotFoundException(UUID.randomUUID())).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(errors.forbidden(new ReservationAccessDeniedException()).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    private static PlaceReservationRequest request(int daysFromToday, String email) {
        return request(daysFromToday, email, 2);
    }

    private static PlaceReservationRequest request(int daysFromToday, String email, int guests) {
        LocalDate checkIn = TODAY.plusDays(daysFromToday);
        return new PlaceReservationRequest(HOTEL_ID, ROOM_ID, "Alex Guest", email, checkIn,
                checkIn.plusDays(2), guests, 1);
    }

    private int roomCount(String roomTypeId, LocalDate date) {
        return jdbc.queryForObject("SELECT available_rooms FROM inventory_days WHERE room_type_id = ? AND inventory_date = ?",
                Integer.class, roomTypeId, java.sql.Date.valueOf(date));
    }
}
