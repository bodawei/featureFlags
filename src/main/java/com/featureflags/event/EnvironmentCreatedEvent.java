package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record EnvironmentCreatedEvent(
    UUID environmentId, String name, String description, Instant occurredAt)
    implements EnvironmentEvent {}
