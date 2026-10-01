package com.wayfarer.reservation.application;

import com.wayfarer.reservation.domain.ReservationFunnelEvent;

public interface ReservationFunnelEventStore {
    void append(ReservationFunnelEvent event);
}
