package com.featureflags.domain;

import com.featureflags.event.DomainEvent;
import com.featureflags.event.EnvironmentAddedEvent;
import com.featureflags.event.EnvironmentRemovedEvent;
import com.featureflags.event.FeatureFlagCreatedEvent;
import com.featureflags.event.FeatureFlagDeletedEvent;
import com.featureflags.event.FeatureFlagRenamedEvent;
import com.featureflags.event.VariantAddedEvent;
import com.featureflags.event.VariantModifiedEvent;
import com.featureflags.event.VariantRemovedEvent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeatureFlag {

  private UUID id;
  private String name;
  private boolean deleted;
  private final Map<String, Environment> environments = new LinkedHashMap<>();
  private final Map<UUID, Variant> variants = new LinkedHashMap<>();

  private FeatureFlag() {}

  public static FeatureFlag reconstitute(List<DomainEvent> events) {
    var flag = new FeatureFlag();
    for (var event : events) {
      flag.apply(event);
    }
    return flag;
  }

  private void apply(DomainEvent event) {
    switch (event) {
      case FeatureFlagCreatedEvent e -> {
        this.id = e.flagId();
        this.name = e.name();
        this.deleted = false;
      }
      case FeatureFlagDeletedEvent e -> this.deleted = true;
      case FeatureFlagRenamedEvent e -> this.name = e.newName();
      case EnvironmentAddedEvent e ->
          environments.put(e.environmentName(), new Environment(e.environmentName()));
      case EnvironmentRemovedEvent e -> environments.remove(e.environmentName());
      case VariantAddedEvent e ->
          variants.put(e.variantId(), new Variant(e.variantId(), e.name(), e.value()));
      case VariantRemovedEvent e -> variants.remove(e.variantId());
      case VariantModifiedEvent e ->
          variants.put(e.variantId(), new Variant(e.variantId(), e.name(), e.value()));
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

  public boolean hasEnvironment(String name) {
    return environments.containsKey(name);
  }

  public boolean hasVariant(UUID variantId) {
    return variants.containsKey(variantId);
  }

  public Map<String, Environment> environments() {
    return Collections.unmodifiableMap(environments);
  }

  public Map<UUID, Variant> variants() {
    return Collections.unmodifiableMap(variants);
  }
}
