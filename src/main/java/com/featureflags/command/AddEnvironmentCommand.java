package com.featureflags.command;

import java.util.UUID;

public record AddEnvironmentCommand(UUID flagId, String environmentName) {
  public AddEnvironmentCommand {
    CommandValidation.requireNonNull(flagId, "flagId");
    CommandValidation.requireNonBlank(environmentName, "environmentName");
  }
}
