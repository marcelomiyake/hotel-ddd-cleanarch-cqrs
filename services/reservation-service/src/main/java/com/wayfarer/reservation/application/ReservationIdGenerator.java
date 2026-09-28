package com.wayfarer.reservation.application;

import java.util.UUID;

@FunctionalInterface
public interface ReservationIdGenerator {
    UUID nextId();
}
