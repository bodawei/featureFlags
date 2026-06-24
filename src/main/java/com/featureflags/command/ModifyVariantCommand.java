package com.featureflags.command;

import java.util.UUID;

public record ModifyVariantCommand(UUID flagId, UUID variantId, String name, String value) {
  public ModifyVariantCommand {
    CommandValidation.requireNonNull(flagId, "flagId");
    CommandValidation.requireNonNull(variantId, "variantId");
    CommandValidation.requireNonBlank(name, "name");
    CommandValidation.requireNonBlank(value, "value");
  }
}
