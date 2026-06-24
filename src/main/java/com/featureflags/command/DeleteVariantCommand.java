package com.featureflags.command;

import java.util.UUID;

public record DeleteVariantCommand(UUID variantId) {
  public DeleteVariantCommand {
    CommandValidation.requireNonNull(variantId, "variantId");
  }
}
