package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record FeatureFlagCreatedEvent(UUID flagId, String name, Instant occurredAt) implements DomainEvent {}
