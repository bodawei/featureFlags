package com.featureflags.handler;

import java.util.UUID;

/** A flag that references an environment or variant, returned by usage lookups. */
public record FlagReference(UUID flagId, String name) {}
