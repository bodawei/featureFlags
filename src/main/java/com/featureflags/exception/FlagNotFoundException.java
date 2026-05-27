package com.featureflags.exception;

import java.util.UUID;

public class FlagNotFoundException extends RuntimeException {
    public FlagNotFoundException(UUID flagId) {
        super("Feature flag not found: " + flagId);
    }
}
