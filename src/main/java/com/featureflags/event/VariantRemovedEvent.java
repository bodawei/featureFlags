package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record VariantRemovedEvent(UUID flagId, UUID variantId, Instant occurredAt)
    implements DomainEvent {}
