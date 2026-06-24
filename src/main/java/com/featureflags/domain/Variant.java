package com.featureflags.domain;

import com.featureflags.event.DomainEvent;
import com.featureflags.event.VariantCreatedEvent;
import com.featureflags.event.VariantDeletedEvent;
import com.featureflags.event.VariantEvent;
import com.featureflags.event.VariantUpdatedEvent;
import java.util.List;
import java.util.UUID;

public class Variant {

  private UUID id;
  private String value;
  private boolean deleted;

  private Variant() {}

  public static Variant reconstitute(List<DomainEvent> events) {
    var variant = new Variant();
    for (var event : events) {
      if (event instanceof VariantEvent variantEvent) {
        variant.apply(variantEvent);
      }
    }
    return variant;
  }

  private void apply(VariantEvent event) {
    switch (event) {
      case VariantCreatedEvent e -> {
        this.id = e.variantId();
        this.value = e.value();
        this.deleted = false;
      }
      case VariantUpdatedEvent e -> {
        if (e.value() != null) this.value = e.value();
      }
      case VariantDeletedEvent e -> this.deleted = true;
    }
  }

  public UUID id() {
    return id;
  }

  public String value() {
    return value;
  }

  public boolean isDeleted() {
    return deleted;
  }
}
