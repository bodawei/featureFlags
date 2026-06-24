package com.featureflags.command;

import java.util.UUID;

public record DeleteEnvironmentCommand(UUID environmentId) {
  public DeleteEnvironmentCommand {
    CommandValidation.requireNonNull(environmentId, "environmentId");
  }
}
