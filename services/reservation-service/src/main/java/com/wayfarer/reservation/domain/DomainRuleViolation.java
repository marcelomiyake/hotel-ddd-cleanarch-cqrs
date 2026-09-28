package com.wayfarer.reservation.domain;

public class DomainRuleViolation extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DomainRuleViolation(String message) {
        super(message);
    }
}
