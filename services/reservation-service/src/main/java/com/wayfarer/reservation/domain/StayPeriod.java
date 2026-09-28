package com.wayfarer.reservation.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

public record StayPeriod(LocalDate checkIn, LocalDate checkOut) {
    private static final int MAX_NIGHTS = 30;

    public StayPeriod {
        Objects.requireNonNull(checkIn, "Check-in date is required.");
        Objects.requireNonNull(checkOut, "Check-out date is required.");
        if (!checkOut.isAfter(checkIn)) {
            throw new DomainRuleViolation("Check-out must be after check-in.");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > MAX_NIGHTS) {
            throw new DomainRuleViolation("A stay cannot exceed 30 nights.");
        }
    }

    public int nights() {
        return Math.toIntExact(ChronoUnit.DAYS.between(checkIn, checkOut));
    }

    public Stream<LocalDate> nightsInRange() {
        return checkIn.datesUntil(checkOut);
    }

    public boolean startsBefore(LocalDate date) {
        return checkIn.isBefore(date);
    }
}
