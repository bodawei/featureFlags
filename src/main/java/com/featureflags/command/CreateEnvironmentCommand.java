package com.featureflags.command;

public record CreateEnvironmentCommand(String name, String description) {
  public CreateEnvironmentCommand {
    CommandValidation.requireNonBlank(name, "name");
  }
}
