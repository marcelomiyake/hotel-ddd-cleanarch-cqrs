package com.wayfarer.reservation.adapter.out.postgres;

import com.wayfarer.reservation.application.TransactionBoundary;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

public class SpringTransactionBoundary implements TransactionBoundary {
    private final TransactionTemplate template;

    public SpringTransactionBoundary(TransactionTemplate template) {
        this.template = template;
    }

    @Override
    public <T> T inTransaction(Supplier<T> operation) {
        return template.execute(status -> operation.get());
    }
}
