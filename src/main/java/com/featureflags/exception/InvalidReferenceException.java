package com.featureflags.exception;

import java.util.UUID;

/** Raised when a flag create/update references an environment or variant that does not exist. */
public class InvalidReferenceException extends FeatureFlagDomainException {
  public InvalidReferenceException(String referenceType, UUID id) {
    super("Unknown " + referenceType + " reference: " + id);
  }
}
