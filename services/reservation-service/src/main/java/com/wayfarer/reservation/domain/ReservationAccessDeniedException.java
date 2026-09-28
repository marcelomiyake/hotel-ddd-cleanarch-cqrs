package com.wayfarer.reservation.domain;

public final class ReservationAccessDeniedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReservationAccessDeniedException() {
        super("The guest email does not match this reservation.");
    }
}
