package com.featureflags.exception;

public abstract class FeatureFlagDomainException extends RuntimeException {
    protected FeatureFlagDomainException(String message) {
        super(message);
    }
}
