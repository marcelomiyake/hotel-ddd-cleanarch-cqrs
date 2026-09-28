package com.wayfarer.reservation.application;

import java.time.LocalDate;

public record AvailabilityQuery(LocalDate checkIn, LocalDate checkOut, int guests) {
}
