package com.featureflags.command;

import java.util.UUID;

final class CommandValidation {

  private CommandValidation() {}

  static void requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
  }

  static void requireNonNull(UUID value, String field) {
    if (value == null) {
      throw new IllegalArgumentException(field + " must not be null");
    }
  }
}
