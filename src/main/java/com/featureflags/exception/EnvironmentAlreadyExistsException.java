package com.featureflags.exception;

import java.util.UUID;

public class EnvironmentAlreadyExistsException extends RuntimeException {
    public EnvironmentAlreadyExistsException(UUID flagId, String environmentName) {
        super("Environment '" + environmentName + "' already exists on flag: " + flagId);
    }
}
