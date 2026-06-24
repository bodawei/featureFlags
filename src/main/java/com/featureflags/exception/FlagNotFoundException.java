package com.featureflags.exception;

import java.util.UUID;

public class FlagNotFoundException extends FeatureFlagDomainException {
    public FlagNotFoundException(UUID flagId) {
        super("Feature flag not found: " + flagId);
    }
}
