package com.featureflags.event;

import java.util.UUID;

public sealed interface VariantEvent extends DomainEvent
    permits VariantCreatedEvent, VariantUpdatedEvent, VariantDeletedEvent {

  UUID variantId();

  @Override
  default UUID aggregateId() {
    return variantId();
  }
}
