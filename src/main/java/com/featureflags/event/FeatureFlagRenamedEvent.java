package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record FeatureFlagRenamedEvent(UUID flagId, String oldName, String newName, Instant occurredAt) implements DomainEvent {}
