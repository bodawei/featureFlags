package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record EnvironmentDeletedEvent(UUID environmentId, Instant occurredAt)
    implements EnvironmentEvent {}
