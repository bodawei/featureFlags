package com.featureflags.exception;

import java.util.UUID;

public class VariantNotFoundException extends FeatureFlagDomainException {
  public VariantNotFoundException(UUID flagId, UUID variantId) {
    super("Variant '" + variantId + "' not found on flag: " + flagId);
  }
}
