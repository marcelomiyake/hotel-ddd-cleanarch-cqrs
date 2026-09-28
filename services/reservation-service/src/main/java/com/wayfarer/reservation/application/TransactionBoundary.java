package com.wayfarer.reservation.application;

import java.util.function.Supplier;

public interface TransactionBoundary {
    <T> T inTransaction(Supplier<T> operation);
}
