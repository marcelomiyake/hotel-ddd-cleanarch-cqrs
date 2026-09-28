package com.wayfarer.reservation.domain;

public final class RoomUnavailableException extends DomainRuleViolation {
    private static final long serialVersionUID = 1L;

    public RoomUnavailableException(String roomTypeId) {
        super("The selected room is no longer available for every night of this stay: " + roomTypeId);
    }
}
