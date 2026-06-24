package com.featureflags.event;

import java.util.UUID;

public sealed interface EnvironmentEvent extends DomainEvent
    permits EnvironmentCreatedEvent, EnvironmentUpdatedEvent, EnvironmentDeletedEvent {

  UUID environmentId();

  @Override
  default UUID aggregateId() {
    return environmentId();
  }
}
