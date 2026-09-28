package com.wayfarer.reservation.domain;

public final class IdempotencyConflictException extends DomainRuleViolation {
    private static final long serialVersionUID = 1L;

    public IdempotencyConflictException() {
        super("This idempotency key was already used for a different reservation request.");
    }
}
