package com.featureflags.store;

import com.featureflags.exception.FeatureFlagDomainException;

import java.util.UUID;

public class OptimisticConcurrencyException extends FeatureFlagDomainException {
    public OptimisticConcurrencyException(UUID aggregateId, long expected, long actual) {
        super("Concurrent modification on aggregate " + aggregateId
                + ": expected version " + expected + " but was " + actual);
    }
}
