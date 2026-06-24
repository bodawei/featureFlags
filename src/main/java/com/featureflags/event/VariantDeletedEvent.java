package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public record VariantDeletedEvent(UUID variantId, Instant occurredAt) implements VariantEvent {}
