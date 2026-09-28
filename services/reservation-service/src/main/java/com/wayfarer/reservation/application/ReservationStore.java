package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.Reservation;

import java.util.Optional;

public interface ReservationStore {
    void lockIdempotencyKey(String key);

    Optional<Reservation> findByIdempotencyKey(String key);

    Reservation save(Reservation reservation);
}
