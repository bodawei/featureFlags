package com.featureflags.handler;

import com.featureflags.command.CreateEnvironmentCommand;
import com.featureflags.command.DeleteEnvironmentCommand;
import com.featureflags.command.UpdateEnvironmentCommand;
import com.featureflags.domain.Environment;
import com.featureflags.domain.FeatureFlag;
import com.featureflags.event.EnvironmentCreatedEvent;
import com.featureflags.event.EnvironmentDeletedEvent;
import com.featureflags.event.EnvironmentUpdatedEvent;
import com.featureflags.event.FlagUpdatedEvent;
import com.featureflags.exception.EnvironmentNotFoundException;
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
public class EnvironmentCommandHandler {

  private final EventStore eventStore;
  private final FlagReferenceIndex referenceIndex;

  public EnvironmentCommandHandler(EventStore eventStore, FlagReferenceIndex referenceIndex) {
    this.eventStore = eventStore;
    this.referenceIndex = referenceIndex;
  }

  public UUID handle(CreateEnvironmentCommand cmd) {
    UUID environmentId = UUID.randomUUID();
    eventStore.append(
        environmentId,
        List.of(
            new EnvironmentCreatedEvent(
                environmentId, cmd.name(), cmd.description(), Instant.now())),
        0);
    return environmentId;
  }

  public void handle(UpdateEnvironmentCommand cmd) {
    var loaded = loadActive(cmd.environmentId());
    eventStore.append(
        cmd.environmentId(),
        List.of(
            new EnvironmentUpdatedEvent(
                cmd.environmentId(), cmd.name(), cmd.description(), Instant.now())),
        loaded.version());
  }

  public void handle(DeleteEnvironmentCommand cmd) {
    var loaded = loadActive(cmd.environmentId());
    for (UUID flagId : referenceIndex.flagsUsingEnvironment(cmd.environmentId())) {
      stripEnvironment(flagId, cmd.environmentId());
    }
    eventStore.append(
        cmd.environmentId(),
        List.of(new EnvironmentDeletedEvent(cmd.environmentId(), Instant.now())),
        loaded.version());
  }

  /** Flags currently referencing this environment. Throws if the environment does not exist. */
  public List<FlagReference> usage(UUID environmentId) {
    loadActive(environmentId);
    return referenceIndex.flagsUsingEnvironment(environmentId).stream()
        .map(this::toFlagReference)
        .filter(Objects::nonNull)
        .toList();
  }

  private void stripEnvironment(UUID flagId, UUID environmentId) {
    var stream = eventStore.load(flagId);
    var flag = FeatureFlag.reconstitute(stream.events());
    if (flag.id() == null || flag.isDeleted() || !flag.environmentIds().contains(environmentId)) {
      return;
    }
    Set<UUID> remaining = new LinkedHashSet<>(flag.environmentIds());
    remaining.remove(environmentId);
    eventStore.append(
        flagId,
        List.of(new FlagUpdatedEvent(flagId, null, remaining, null, Instant.now())),
        stream.version());
  }

  private FlagReference toFlagReference(UUID flagId) {
    var flag = FeatureFlag.reconstitute(eventStore.load(flagId).events());
    if (flag.id() == null || flag.isDeleted()) return null;
    return new FlagReference(flagId, flag.name());
  }

  private record Loaded(Environment environment, long version) {}

  private Loaded loadActive(UUID environmentId) {
    var stream = eventStore.load(environmentId);
    var environment = Environment.reconstitute(stream.events());
    if (environment.id() == null || environment.isDeleted()) {
      throw new EnvironmentNotFoundException(environmentId);
    }
    return new Loaded(environment, stream.version());
  }
}
