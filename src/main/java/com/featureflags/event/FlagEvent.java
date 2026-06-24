package com.featureflags.event;

import java.util.UUID;

public sealed interface FlagEvent extends DomainEvent
    permits FeatureFlagCreatedEvent, FlagUpdatedEvent, FeatureFlagDeletedEvent {

  UUID flagId();

  @Override
  default UUID aggregateId() {
    return flagId();
  }
}
