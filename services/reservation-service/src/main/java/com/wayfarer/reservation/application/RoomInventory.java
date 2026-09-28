package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.RoomAvailability;
import com.wayfarer.reservation.domain.StayPeriod;

import java.util.List;
import java.util.Optional;

public interface RoomInventory {
    List<RoomAvailability> search(StayPeriod stay, int guests);

    Optional<RoomAvailability> findAvailability(String roomTypeId, StayPeriod stay);

    void reserve(String roomTypeId, StayPeriod stay, int rooms);

    void release(String roomTypeId, StayPeriod stay, int rooms);
}
