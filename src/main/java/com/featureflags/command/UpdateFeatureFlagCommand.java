package com.featureflags.command;

import java.util.Set;
import java.util.UUID;

/**
 * Declarative partial update of a flag. A {@code null} field is left unchanged; a non-null field
 * replaces the current value/set.
 */
public record UpdateFeatureFlagCommand(
    UUID flagId, String name, Set<UUID> environmentIds, Set<UUID> variantIds) {
  public UpdateFeatureFlagCommand {
    CommandValidation.requireNonNull(flagId, "flagId");
    if (name != null) CommandValidation.requireNonBlank(name, "name");
    environmentIds = environmentIds == null ? null : Set.copyOf(environmentIds);
    variantIds = variantIds == null ? null : Set.copyOf(variantIds);
  }
}
