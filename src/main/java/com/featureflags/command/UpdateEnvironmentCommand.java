package com.featureflags.command;

import java.util.UUID;

/** A {@code null} field is left unchanged; a non-null field replaces the current value. */
public record UpdateEnvironmentCommand(UUID environmentId, String name, String description) {
  public UpdateEnvironmentCommand {
    CommandValidation.requireNonNull(environmentId, "environmentId");
    if (name != null) CommandValidation.requireNonBlank(name, "name");
  }
}
