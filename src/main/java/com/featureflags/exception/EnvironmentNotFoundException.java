package com.featureflags.exception;

import java.util.UUID;

public class EnvironmentNotFoundException extends FeatureFlagDomainException {
  public EnvironmentNotFoundException(UUID flagId, String environmentName) {
    super("Environment '" + environmentName + "' not found on flag: " + flagId);
  }
}
