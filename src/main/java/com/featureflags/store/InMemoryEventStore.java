package com.featureflags.store;

import com.featureflags.event.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryEventStore implements EventStore {

    private final ConcurrentHashMap<UUID, List<DomainEvent>> store = new ConcurrentHashMap<>();

    @Override
    public void append(UUID aggregateId, List<DomainEvent> events, long expectedVersion) {
        store.compute(aggregateId, (id, existing) -> {
            List<DomainEvent> current = existing != null ? existing : new ArrayList<>();
            if (current.size() != expectedVersion) {
                throw new OptimisticConcurrencyException(aggregateId, expectedVersion, current.size());
            }
            current.addAll(events);
            return current;
        });
    }

    @Override
    public EventStream load(UUID aggregateId) {
        List<DomainEvent> events = List.copyOf(store.getOrDefault(aggregateId, List.of()));
        return new EventStream(events, events.size());
    }
}
