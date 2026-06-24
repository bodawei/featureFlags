package com.featureflags.exception;

import java.util.UUID;

public class EnvironmentNotFoundException extends FeatureFlagDomainException {
  public EnvironmentNotFoundException(UUID environmentId) {
    super("Environment not found: " + environmentId);
  }
}
