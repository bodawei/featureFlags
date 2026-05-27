package com.featureflags.store;

import java.util.UUID;

public class OptimisticConcurrencyException extends RuntimeException {
    public OptimisticConcurrencyException(UUID aggregateId, long expected, long actual) {
        super("Concurrent modification on aggregate " + aggregateId
                + ": expected version " + expected + " but was " + actual);
    }
}
