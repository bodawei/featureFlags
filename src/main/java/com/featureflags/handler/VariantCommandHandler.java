package com.featureflags.handler;

import com.featureflags.command.CreateVariantCommand;
import com.featureflags.command.DeleteVariantCommand;
import com.featureflags.command.UpdateVariantCommand;
import com.featureflags.domain.FeatureFlag;
import com.featureflags.domain.Variant;
import com.featureflags.event.FlagUpdatedEvent;
import com.featureflags.event.VariantCreatedEvent;
import com.featureflags.event.VariantDeletedEvent;
import com.featureflags.event.VariantUpdatedEvent;
import com.featureflags.exception.VariantNotFoundException;
import com.featureflags.projection.FlagReferenceIndex;
import com.featureflags.store.EventStore;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class VariantCommandHandler {

  private final EventStore eventStore;
  private final FlagReferenceIndex referenceIndex;

  public VariantCommandHandler(EventStore eventStore, FlagReferenceIndex referenceIndex) {
    this.eventStore = eventStore;
    this.referenceIndex = referenceIndex;
  }

  public UUID handle(CreateVariantCommand cmd) {
    UUID variantId = UUID.randomUUID();
    eventStore.append(
        variantId,
        List.of(new VariantCreatedEvent(variantId, cmd.value(), Instant.now())),
        0);
    return variantId;
  }

  public void handle(UpdateVariantCommand cmd) {
    var loaded = loadActive(cmd.variantId());
    eventStore.append(
        cmd.variantId(),
        List.of(new VariantUpdatedEvent(cmd.variantId(), cmd.value(), Instant.now())),
        loaded.version());
  }

  public void handle(DeleteVariantCommand cmd) {
    var loaded = loadActive(cmd.variantId());
    for (UUID flagId : referenceIndex.flagsUsingVariant(cmd.variantId())) {
      stripVariant(flagId, cmd.variantId());
    }
    eventStore.append(
        cmd.variantId(),
        List.of(new VariantDeletedEvent(cmd.variantId(), Instant.now())),
        loaded.version());
  }

  /** Flags currently referencing this variant. Throws if the variant does not exist. */
  public List<FlagReference> usage(UUID variantId) {
    loadActive(variantId);
    return referenceIndex.flagsUsingVariant(variantId).stream()
        .map(this::toFlagReference)
        .filter(Objects::nonNull)
        .toList();
  }

  private void stripVariant(UUID flagId, UUID variantId) {
    var stream = eventStore.load(flagId);
    var flag = FeatureFlag.reconstitute(stream.events());
    if (flag.id() == null || flag.isDeleted() || !flag.variantIds().contains(variantId)) {
      return;
    }
    Set<UUID> remaining = new LinkedHashSet<>(flag.variantIds());
    remaining.remove(variantId);
    eventStore.append(
        flagId,
        List.of(new FlagUpdatedEvent(flagId, null, null, remaining, Instant.now())),
        stream.version());
  }

  private FlagReference toFlagReference(UUID flagId) {
    var flag = FeatureFlag.reconstitute(eventStore.load(flagId).events());
    if (flag.id() == null || flag.isDeleted()) return null;
    return new FlagReference(flagId, flag.name());
  }

  private record Loaded(Variant variant, long version) {}

  private Loaded loadActive(UUID variantId) {
    var stream = eventStore.load(variantId);
    var variant = Variant.reconstitute(stream.events());
    if (variant.id() == null || variant.isDeleted()) {
      throw new VariantNotFoundException(variantId);
    }
    return new Loaded(variant, stream.version());
  }
}
