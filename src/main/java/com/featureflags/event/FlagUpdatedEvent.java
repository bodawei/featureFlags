package com.featureflags.event;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * A single mutation to a flag. Any of {@code name}, {@code environmentIds}, {@code variantIds} may
 * be {@code null}, meaning that field is left unchanged; a non-null value replaces it.
 */
public record FlagUpdatedEvent(
    UUID flagId, String name, Set<UUID> environmentIds, Set<UUID> variantIds, Instant occurredAt)
    implements FlagEvent {}
