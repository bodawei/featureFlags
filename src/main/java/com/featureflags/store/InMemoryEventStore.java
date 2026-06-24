package com.featureflags.store;

import com.featureflags.event.DomainEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class InMemoryEventStore implements EventStore {

  private final ConcurrentHashMap<UUID, List<DomainEvent>> store = new ConcurrentHashMap<>();
  private final List<Consumer<List<DomainEvent>>> listeners = new CopyOnWriteArrayList<>();

  @Override
  public void append(UUID aggregateId, List<DomainEvent> events, long expectedVersion) {
    store.compute(
        aggregateId,
        (id, existing) -> {
          int currentSize = existing != null ? existing.size() : 0;
          if (currentSize != expectedVersion) {
            throw new OptimisticConcurrencyException(aggregateId, expectedVersion, currentSize);
          }
          List<DomainEvent> updated = new ArrayList<>(currentSize + events.size());
          if (existing != null) updated.addAll(existing);
          updated.addAll(events);
          return List.copyOf(updated);
        });
    for (var listener : listeners) {
      listener.accept(events);
    }
  }

  @Override
  public EventStream load(UUID aggregateId) {
    List<DomainEvent> events = store.getOrDefault(aggregateId, List.of());
    return new EventStream(events, events.size());
  }

  @Override
  public void addListener(Consumer<List<DomainEvent>> listener) {
    listeners.add(listener);
  }
}
