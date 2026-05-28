package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record EnvironmentAddedEvent(UUID flagId, String environmentName, Instant occurredAt) implements DomainEvent {}
