package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

/**
 * A mutation to an environment. Any of {@code name}, {@code description} may be {@code null},
 * meaning that field is left unchanged; a non-null value replaces it.
 */
public record EnvironmentUpdatedEvent(
    UUID environmentId, String name, String description, Instant occurredAt)
    implements EnvironmentEvent {}
