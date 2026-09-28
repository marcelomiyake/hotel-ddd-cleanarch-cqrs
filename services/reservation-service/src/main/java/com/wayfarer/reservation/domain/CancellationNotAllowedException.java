package com.wayfarer.reservation.domain;

public final class CancellationNotAllowedException extends DomainRuleViolation {
    private static final long serialVersionUID = 1L;

    public CancellationNotAllowedException(String reason) {
        super(reason);
    }
}
