package com.featureflags.store;

import com.featureflags.event.DomainEvent;

import java.util.List;

public record EventStream(List<DomainEvent> events, long version) {}
