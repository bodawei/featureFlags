package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface DomainEvent
    permits FeatureFlagCreatedEvent,
        FeatureFlagDeletedEvent,
        FeatureFlagRenamedEvent,
        EnvironmentAddedEvent,
        EnvironmentRemovedEvent,
        VariantAddedEvent,
        VariantRemovedEvent,
        VariantModifiedEvent {

  UUID flagId();

  Instant occurredAt();
}
