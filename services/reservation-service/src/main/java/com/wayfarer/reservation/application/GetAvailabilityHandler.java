package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.DomainRuleViolation;
import com.wayfarer.reservation.domain.RoomAvailability;
import com.wayfarer.reservation.domain.StayPeriod;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class GetAvailabilityHandler {
    private final RoomInventory inventory;
    private final Clock clock;

    public GetAvailabilityHandler(RoomInventory inventory, Clock clock) {
        this.inventory = Objects.requireNonNull(inventory);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<RoomAvailability> handle(AvailabilityQuery query) {
        Objects.requireNonNull(query);
        StayPeriod stay = new StayPeriod(query.checkIn(), query.checkOut());
        if (query.guests() < 1 || query.guests() > 12) {
            throw new DomainRuleViolation("Guest count must be between 1 and 12.");
        }
        if (stay.startsBefore(LocalDate.now(clock))) {
            throw new DomainRuleViolation("Check-in cannot be in the past.");
        }
        return List.copyOf(inventory.search(stay, query.guests()));
    }
}
