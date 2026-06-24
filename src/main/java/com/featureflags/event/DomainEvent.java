package com.featureflags.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface DomainEvent permits FlagEvent, EnvironmentEvent, VariantEvent {

  UUID aggregateId();

  Instant occurredAt();
}
