package com.featureflags.domain;

import com.featureflags.event.DomainEvent;
import com.featureflags.event.EnvironmentCreatedEvent;
import com.featureflags.event.EnvironmentDeletedEvent;
import com.featureflags.event.EnvironmentEvent;
import com.featureflags.event.EnvironmentUpdatedEvent;
import java.util.List;
import java.util.UUID;

public class Environment {

  private UUID id;
  private String name;
  private String description;
  private boolean deleted;

  private Environment() {}

  public static Environment reconstitute(List<DomainEvent> events) {
    var environment = new Environment();
    for (var event : events) {
      if (event instanceof EnvironmentEvent environmentEvent) {
        environment.apply(environmentEvent);
      }
    }
    return environment;
  }

  private void apply(EnvironmentEvent event) {
    switch (event) {
      case EnvironmentCreatedEvent e -> {
        this.id = e.environmentId();
        this.name = e.name();
        this.description = e.description();
        this.deleted = false;
      }
      case EnvironmentUpdatedEvent e -> {
        if (e.name() != null) this.name = e.name();
        if (e.description() != null) this.description = e.description();
      }
      case EnvironmentDeletedEvent e -> this.deleted = true;
    }
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String description() {
    return description;
  }

  public boolean isDeleted() {
    return deleted;
  }
}
