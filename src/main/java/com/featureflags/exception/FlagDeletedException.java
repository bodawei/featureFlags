package com.featureflags.exception;

import java.util.UUID;

public class FlagDeletedException extends FeatureFlagDomainException {
    public FlagDeletedException(UUID flagId) {
        super("Feature flag has been deleted: " + flagId);
    }
}
