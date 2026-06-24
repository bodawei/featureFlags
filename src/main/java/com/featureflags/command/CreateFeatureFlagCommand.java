package com.featureflags.command;

public record CreateFeatureFlagCommand(String name) {
  public CreateFeatureFlagCommand {
    CommandValidation.requireNonBlank(name, "name");
  }
}
