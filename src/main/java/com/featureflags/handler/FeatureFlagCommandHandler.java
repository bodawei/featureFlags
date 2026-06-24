package com.featureflags.handler;

import com.featureflags.command.CreateFeatureFlagCommand;
import com.featureflags.command.DeleteFeatureFlagCommand;
import com.featureflags.command.UpdateFeatureFlagCommand;
import com.featureflags.domain.Environment;
import com.featureflags.domain.FeatureFlag;
import com.featureflags.domain.Variant;
import com.featureflags.event.FeatureFlagCreatedEvent;
import com.featureflags.event.FeatureFlagDeletedEvent;
import com.featureflags.event.FlagUpdatedEvent;
import com.featureflags.exception.FlagDeletedException;
import com.featureflags.exception.FlagNotFoundException;
import com.featureflags.exception.InvalidReferenceException;
import com.featureflags.store.EventStore;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagCommandHandler {

  private final EventStore eventStore;

  public FeatureFlagCommandHandler(EventStore eventStore) {
    this.eventStore = eventStore;
  }

  public UUID handle(CreateFeatureFlagCommand cmd) {
    validateEnvironments(cmd.environmentIds());
    validateVariants(cmd.variantIds());
    UUID flagId = UUID.randomUUID();
    eventStore.append(
        flagId,
        List.of(
            new FeatureFlagCreatedEvent(
                flagId, cmd.name(), cmd.environmentIds(), cmd.variantIds(), Instant.now())),
        0);
    return flagId;
  }

  public void handle(UpdateFeatureFlagCommand cmd) {
    var loaded = loadActive(cmd.flagId());
    if (cmd.environmentIds() != null) validateEnvironments(cmd.environmentIds());
    if (cmd.variantIds() != null) validateVariants(cmd.variantIds());
    eventStore.append(
        cmd.flagId(),
        List.of(
            new FlagUpdatedEvent(
                cmd.flagId(),
                cmd.name(),
                cmd.environmentIds(),
                cmd.variantIds(),
                Instant.now())),
        loaded.version());
  }

  public void handle(DeleteFeatureFlagCommand cmd) {
    var loaded = loadActive(cmd.flagId());
    eventStore.append(
        cmd.flagId(),
        List.of(new FeatureFlagDeletedEvent(cmd.flagId(), Instant.now())),
        loaded.version());
  }

  private void validateEnvironments(Set<UUID> environmentIds) {
    for (UUID environmentId : environmentIds) {
      var environment = Environment.reconstitute(eventStore.load(environmentId).events());
      if (environment.id() == null || environment.isDeleted()) {
        throw new InvalidReferenceException("environment", environmentId);
      }
    }
  }

  private void validateVariants(Set<UUID> variantIds) {
    for (UUID variantId : variantIds) {
      var variant = Variant.reconstitute(eventStore.load(variantId).events());
      if (variant.id() == null || variant.isDeleted()) {
        throw new InvalidReferenceException("variant", variantId);
      }
    }
  }

  private record Loaded(FeatureFlag flag, long version) {}

  private Loaded loadActive(UUID flagId) {
    var stream = eventStore.load(flagId);
    var flag = FeatureFlag.reconstitute(stream.events());
    if (flag.id() == null) throw new FlagNotFoundException(flagId);
    if (flag.isDeleted()) throw new FlagDeletedException(flagId);
    return new Loaded(flag, stream.version());
  }
}
