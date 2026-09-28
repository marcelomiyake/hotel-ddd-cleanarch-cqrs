package com.wayfarer.reservation.domain;

import java.util.Locale;
import java.util.Objects;

public record GuestDetails(String fullName, String email) {
    public GuestDetails {
        Objects.requireNonNull(fullName, "Guest name is required.");
        Objects.requireNonNull(email, "Guest email is required.");
        fullName = fullName.trim();
        email = email.trim().toLowerCase(Locale.ROOT);
        if (fullName.isBlank() || fullName.length() > 120) {
            throw new DomainRuleViolation("Guest name must contain 1 to 120 characters.");
        }
        if (email.length() > 254 || !isValidEmail(email)) {
            throw new DomainRuleViolation("A valid guest email is required.");
        }
    }

    private static boolean isValidEmail(String email) {
        int separator = email.indexOf('@');
        int domainDot = email.indexOf('.', separator + 1);
        return separator > 0
                && separator == email.lastIndexOf('@')
                && domainDot > separator + 1
                && domainDot < email.length() - 1
                && email.chars().noneMatch(Character::isWhitespace);
    }
}
