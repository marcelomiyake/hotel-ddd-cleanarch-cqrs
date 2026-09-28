package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.Reservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationQueries {
    Optional<Reservation> findById(UUID id);

    List<Reservation> findByGuestEmail(String normalizedEmail);
}
