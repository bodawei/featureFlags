package com.featureflags.store;

import com.featureflags.event.DomainEvent;

import java.util.List;
import java.util.UUID;

public interface EventStore {
    void append(UUID aggregateId, List<DomainEvent> events, long expectedVersion);
    EventStream load(UUID aggregateId);
}
