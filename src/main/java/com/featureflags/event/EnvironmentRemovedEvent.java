package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record EnvironmentRemovedEvent(UUID flagId, String environmentName, Instant occurredAt)
    implements DomainEvent {}
