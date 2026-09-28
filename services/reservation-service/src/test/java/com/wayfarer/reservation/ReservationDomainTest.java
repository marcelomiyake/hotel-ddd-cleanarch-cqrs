package com.wayfarer.reservation;

import com.wayfarer.reservation.domain.CancellationNotAllowedException;
import com.wayfarer.reservation.domain.DomainRuleViolation;
import com.wayfarer.reservation.domain.GuestDetails;
import com.wayfarer.reservation.domain.Reservation;
import com.wayfarer.reservation.domain.ReservationStatus;
import com.wayfarer.reservation.domain.RoomAvailability;
import com.wayfarer.reservation.domain.RoomUnavailableException;
import com.wayfarer.reservation.domain.StayPeriod;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationDomainTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 28);
    private static final StayPeriod STAY = new StayPeriod(TODAY.plusDays(10), TODAY.plusDays(13));
    private static final GuestDetails GUEST = new GuestDetails(" Alex Guest ", " ALEX@example.com ");
    private static final RoomAvailability OFFER = new RoomAvailability(
            "hotel-1", "room-1", "Ocean Room", 3, 2, 148000, "BRL");

    @Test
    void validatesStayDatesAndProvidesNightDates() {
        assertThat(STAY.nights()).isEqualTo(3);
        assertThat(STAY.nightsInRange()).containsExactly(TODAY.plusDays(10), TODAY.plusDays(11), TODAY.plusDays(12));
        assertThat(STAY.startsBefore(TODAY)).isFalse();
        assertThatThrownBy(() -> new StayPeriod(TODAY, TODAY)).isInstanceOf(DomainRuleViolation.class);
        assertThatThrownBy(() -> new StayPeriod(TODAY, TODAY.plusDays(31))).isInstanceOf(DomainRuleViolation.class);
    }

    @Test
    void validatesAndNormalizesGuestDetails() {
        assertThat(GUEST.fullName()).isEqualTo("Alex Guest");
        assertThat(GUEST.email()).isEqualTo("alex@example.com");
        assertThatThrownBy(() -> new GuestDetails(" ", "guest@example.com"))
                .isInstanceOf(DomainRuleViolation.class);
        assertThatThrownBy(() -> new GuestDetails("Guest", "not-email"))
                .isInstanceOf(DomainRuleViolation.class);
    }

    @Test
    void verifiesRoomCapacityInventoryAndPriceArithmetic() {
        assertThat(OFFER.canAccommodate(2, 1)).isTrue();
        assertThat(OFFER.canAccommodate(3, 1)).isFalse();
        assertThat(OFFER.canAccommodate(2, 4)).isFalse();
        assertThat(OFFER.canAccommodate(0, 1)).isFalse();
        assertThat(OFFER.totalPriceCents(STAY, 2)).isEqualTo(888000);
        assertThatThrownBy(() -> new RoomAvailability("hotel", "room", "Room", -1, 2, 100, "BRL"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RoomAvailability("hotel", "room", "Room", 1, 0, 100, "BRL"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RoomAvailability("hotel", "room", "Room", 1, 2, Long.MAX_VALUE, "BRL")
                .totalPriceCents(STAY, 3)).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void placesAndCancelsAValidReservation() {
        Reservation reservation = Reservation.place(UUID.randomUUID(), "request-1", "hotel-1", "room-1",
                GUEST, STAY, 2, 1, OFFER, TODAY, Instant.parse("2026-09-28T12:00:00Z"));
        assertThat(reservation.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.totalPriceCents()).isEqualTo(444000);
        assertThat(reservation.matches("hotel-1", "room-1", GUEST, STAY, 2, 1)).isTrue();
        assertThat(reservation.matches("hotel-2", "room-1", GUEST, STAY, 2, 1)).isFalse();

        Reservation cancelled = reservation.cancel(TODAY);
        assertThat(cancelled.status()).isEqualTo(ReservationStatus.CANCELLED);
        assertThatThrownBy(() -> cancelled.cancel(TODAY)).isInstanceOf(CancellationNotAllowedException.class);
        assertThatThrownBy(() -> reservation.cancel(STAY.checkIn()))
                .isInstanceOf(CancellationNotAllowedException.class);
    }

    @Test
    void rejectsMismatchedRoomsPastArrivalsAndInsufficientInventory() {
        assertThatThrownBy(() -> Reservation.place(UUID.randomUUID(), "key", "hotel-2", "room-1", GUEST,
                STAY, 2, 1, OFFER, TODAY, Instant.now())).isInstanceOf(DomainRuleViolation.class);
        assertThatThrownBy(() -> Reservation.place(UUID.randomUUID(), "key", "hotel-1", "room-1", GUEST,
                new StayPeriod(TODAY.minusDays(1), TODAY.plusDays(2)), 2, 1, OFFER, TODAY, Instant.now()))
                .isInstanceOf(DomainRuleViolation.class);
        assertThatThrownBy(() -> Reservation.place(UUID.randomUUID(), "key", "hotel-1", "room-1", GUEST,
                STAY, 4, 1, OFFER, TODAY, Instant.now())).isInstanceOf(RoomUnavailableException.class);
        assertThatThrownBy(() -> new Reservation(UUID.randomUUID(), " ", "hotel-1", "room-1", "Room", GUEST,
                STAY, 2, 1, ReservationStatus.CONFIRMED, 1, 3, "BRL", Instant.now()))
                .isInstanceOf(DomainRuleViolation.class);
    }
}
