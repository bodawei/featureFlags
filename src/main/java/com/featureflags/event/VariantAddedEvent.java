package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record VariantAddedEvent(
    UUID flagId, UUID variantId, String name, String value, Instant occurredAt)
    implements DomainEvent {}
