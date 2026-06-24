package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record VariantCreatedEvent(UUID variantId, String value, Instant occurredAt)
    implements VariantEvent {}
