package com.featureflags.command;

import java.util.Set;
import java.util.UUID;

public record CreateFeatureFlagCommand(
    String name, Set<UUID> environmentIds, Set<UUID> variantIds) {
  public CreateFeatureFlagCommand {
    CommandValidation.requireNonBlank(name, "name");
    environmentIds = environmentIds == null ? Set.of() : Set.copyOf(environmentIds);
    variantIds = variantIds == null ? Set.of() : Set.copyOf(variantIds);
  }
}
