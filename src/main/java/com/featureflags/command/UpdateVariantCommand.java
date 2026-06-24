package com.featureflags.command;

import java.util.UUID;

/** A {@code null} value is left unchanged; a non-null value replaces the current value. */
public record UpdateVariantCommand(UUID variantId, String value) {
  public UpdateVariantCommand {
    CommandValidation.requireNonNull(variantId, "variantId");
    if (value != null) CommandValidation.requireNonBlank(value, "value");
  }
}
