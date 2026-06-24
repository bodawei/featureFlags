package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record FeatureFlagDeletedEvent(UUID flagId, Instant occurredAt) implements DomainEvent {}
