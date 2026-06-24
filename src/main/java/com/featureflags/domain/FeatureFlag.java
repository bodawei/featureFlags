package com.featureflags.domain;

import com.featureflags.event.DomainEvent;
import com.featureflags.event.FeatureFlagCreatedEvent;
import com.featureflags.event.FeatureFlagDeletedEvent;
import com.featureflags.event.FlagEvent;
import com.featureflags.event.FlagUpdatedEvent;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FeatureFlag {

  private UUID id;
  private String name;
  private boolean deleted;
  private final Set<UUID> environmentIds = new LinkedHashSet<>();
  private final Set<UUID> variantIds = new LinkedHashSet<>();

  private FeatureFlag() {}

  public static FeatureFlag reconstitute(List<DomainEvent> events) {
    var flag = new FeatureFlag();
    for (var event : events) {
      if (event instanceof FlagEvent flagEvent) {
        flag.apply(flagEvent);
      }
    }
    return flag;
  }

  private void apply(FlagEvent event) {
    switch (event) {
      case FeatureFlagCreatedEvent e -> {
        this.id = e.flagId();
        this.name = e.name();
        this.deleted = false;
        if (e.environmentIds() != null) environmentIds.addAll(e.environmentIds());
        if (e.variantIds() != null) variantIds.addAll(e.variantIds());
      }
      case FlagUpdatedEvent e -> {
        if (e.name() != null) this.name = e.name();
        if (e.environmentIds() != null) {
          environmentIds.clear();
          environmentIds.addAll(e.environmentIds());
        }
        if (e.variantIds() != null) {
          variantIds.clear();
          variantIds.addAll(e.variantIds());
        }
      }
      case FeatureFlagDeletedEvent e -> this.deleted = true;
    }
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public Set<UUID> environmentIds() {
    return Collections.unmodifiableSet(environmentIds);
  }

  public Set<UUID> variantIds() {
    return Collections.unmodifiableSet(variantIds);
  }
}
