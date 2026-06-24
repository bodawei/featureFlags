package com.featureflags.exception;

import java.util.UUID;

public class VariantNotFoundException extends FeatureFlagDomainException {
  public VariantNotFoundException(UUID variantId) {
    super("Variant not found: " + variantId);
  }
}
