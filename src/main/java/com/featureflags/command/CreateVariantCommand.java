package com.featureflags.command;

public record CreateVariantCommand(String value) {
  public CreateVariantCommand {
    CommandValidation.requireNonBlank(value, "value");
  }
}
