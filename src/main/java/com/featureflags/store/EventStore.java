package com.featureflags.store;

import com.featureflags.event.DomainEvent;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface EventStore {
  void append(UUID aggregateId, List<DomainEvent> events, long expectedVersion);

  EventStream load(UUID aggregateId);

  /** Registers a listener invoked with the appended events after each successful append. */
  void addListener(Consumer<List<DomainEvent>> listener);
}
