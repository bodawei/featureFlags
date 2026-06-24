package com.featureflags.projection;

import com.featureflags.event.DomainEvent;
import com.featureflags.event.FeatureFlagCreatedEvent;
import com.featureflags.event.FeatureFlagDeletedEvent;
import com.featureflags.event.FlagUpdatedEvent;
import com.featureflags.store.EventStore;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Read-model projection that tracks which flags reference each environment and variant. Updated
 * synchronously as flag events are appended, so it backs both the {@code usage} lookups and the
 * cascade cleanup performed when an environment or variant is deleted.
 */
@Component
public class FlagReferenceIndex {

  // Current reference sets per flag, kept so we can diff on each update.
  private final Map<UUID, Set<UUID>> flagEnvironments = new ConcurrentHashMap<>();
  private final Map<UUID, Set<UUID>> flagVariants = new ConcurrentHashMap<>();

  // Reverse indexes: referenced id -> flags referencing it.
  private final Map<UUID, Set<UUID>> environmentToFlags = new ConcurrentHashMap<>();
  private final Map<UUID, Set<UUID>> variantToFlags = new ConcurrentHashMap<>();

  private final EventStore eventStore;

  public FlagReferenceIndex(EventStore eventStore) {
    this.eventStore = eventStore;
  }

  @PostConstruct
  void register() {
    eventStore.addListener(this::onEvents);
  }

  private synchronized void onEvents(List<DomainEvent> events) {
    for (var event : events) {
      switch (event) {
        case FeatureFlagCreatedEvent e ->
            updateFlag(e.flagId(), e.environmentIds(), e.variantIds());
        case FlagUpdatedEvent e -> updateFlag(e.flagId(), e.environmentIds(), e.variantIds());
        case FeatureFlagDeletedEvent e -> removeFlag(e.flagId());
        default -> {}
      }
    }
  }

  private void updateFlag(UUID flagId, Set<UUID> newEnvironments, Set<UUID> newVariants) {
    if (newEnvironments != null) {
      reindex(flagEnvironments, environmentToFlags, flagId, newEnvironments);
    }
    if (newVariants != null) {
      reindex(flagVariants, variantToFlags, flagId, newVariants);
    }
  }

  private void removeFlag(UUID flagId) {
    reindex(flagEnvironments, environmentToFlags, flagId, Set.of());
    reindex(flagVariants, variantToFlags, flagId, Set.of());
    flagEnvironments.remove(flagId);
    flagVariants.remove(flagId);
  }

  private static void reindex(
      Map<UUID, Set<UUID>> forward,
      Map<UUID, Set<UUID>> reverse,
      UUID flagId,
      Set<UUID> newReferences) {
    Set<UUID> old = forward.getOrDefault(flagId, Set.of());
    for (UUID referenced : old) {
      if (!newReferences.contains(referenced)) reverseRemove(reverse, referenced, flagId);
    }
    for (UUID referenced : newReferences) {
      reverseAdd(reverse, referenced, flagId);
    }
    forward.put(flagId, Set.copyOf(newReferences));
  }

  private static void reverseAdd(Map<UUID, Set<UUID>> reverse, UUID referenced, UUID flagId) {
    reverse.computeIfAbsent(referenced, k -> ConcurrentHashMap.newKeySet()).add(flagId);
  }

  private static void reverseRemove(Map<UUID, Set<UUID>> reverse, UUID referenced, UUID flagId) {
    Set<UUID> flags = reverse.get(referenced);
    if (flags != null) {
      flags.remove(flagId);
      if (flags.isEmpty()) reverse.remove(referenced);
    }
  }

  public Set<UUID> flagsUsingEnvironment(UUID environmentId) {
    return Set.copyOf(environmentToFlags.getOrDefault(environmentId, Set.of()));
  }

  public Set<UUID> flagsUsingVariant(UUID variantId) {
    return Set.copyOf(variantToFlags.getOrDefault(variantId, Set.of()));
  }
}
