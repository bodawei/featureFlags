package com.featureflags.handler;

import com.featureflags.command.AddEnvironmentCommand;
import com.featureflags.command.AddVariantCommand;
import com.featureflags.command.CreateFeatureFlagCommand;
import com.featureflags.command.DeleteFeatureFlagCommand;
import com.featureflags.command.ModifyVariantCommand;
import com.featureflags.command.RemoveEnvironmentCommand;
import com.featureflags.command.RemoveVariantCommand;
import com.featureflags.command.RenameFeatureFlagCommand;
import com.featureflags.domain.FeatureFlag;
import com.featureflags.event.EnvironmentAddedEvent;
import com.featureflags.event.EnvironmentRemovedEvent;
import com.featureflags.event.FeatureFlagCreatedEvent;
import com.featureflags.event.FeatureFlagDeletedEvent;
import com.featureflags.event.FeatureFlagRenamedEvent;
import com.featureflags.event.VariantAddedEvent;
import com.featureflags.event.VariantModifiedEvent;
import com.featureflags.event.VariantRemovedEvent;
import com.featureflags.exception.EnvironmentAlreadyExistsException;
import com.featureflags.exception.EnvironmentNotFoundException;
import com.featureflags.exception.FlagDeletedException;
import com.featureflags.exception.FlagNotFoundException;
import com.featureflags.exception.VariantNotFoundException;
import com.featureflags.store.EventStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FeatureFlagCommandHandler {

    private final EventStore eventStore;

    public FeatureFlagCommandHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public UUID handle(CreateFeatureFlagCommand cmd) {
        UUID flagId = UUID.randomUUID();
        eventStore.append(flagId, List.of(new FeatureFlagCreatedEvent(flagId, cmd.name(), Instant.now())), 0);
        return flagId;
    }

    public void handle(DeleteFeatureFlagCommand cmd) {
        var loaded = loadActive(cmd.flagId());
        eventStore.append(cmd.flagId(), List.of(new FeatureFlagDeletedEvent(cmd.flagId(), Instant.now())), loaded.version());
    }

    public void handle(RenameFeatureFlagCommand cmd) {
        var loaded = loadActive(cmd.flagId());
        if (cmd.newName().equals(loaded.flag().name())) return;
        eventStore.append(cmd.flagId(), List.of(
                new FeatureFlagRenamedEvent(cmd.flagId(), loaded.flag().name(), cmd.newName(), Instant.now())),
                loaded.version());
    }

    public void handle(AddEnvironmentCommand cmd) {
        var loaded = loadActive(cmd.flagId());
        if (loaded.flag().hasEnvironment(cmd.environmentName())) {
            throw new EnvironmentAlreadyExistsException(cmd.flagId(), cmd.environmentName());
        }
        eventStore.append(cmd.flagId(), List.of(
                new EnvironmentAddedEvent(cmd.flagId(), cmd.environmentName(), Instant.now())),
                loaded.version());
    }

    public void handle(RemoveEnvironmentCommand cmd) {
        var loaded = loadActive(cmd.flagId());
        if (!loaded.flag().hasEnvironment(cmd.environmentName())) {
            throw new EnvironmentNotFoundException(cmd.flagId(), cmd.environmentName());
        }
        eventStore.append(cmd.flagId(), List.of(
                new EnvironmentRemovedEvent(cmd.flagId(), cmd.environmentName(), Instant.now())),
                loaded.version());
    }

    public UUID handle(AddVariantCommand cmd) {
        var loaded = loadActive(cmd.flagId());
        UUID variantId = UUID.randomUUID();
        eventStore.append(cmd.flagId(), List.of(
                new VariantAddedEvent(cmd.flagId(), variantId, cmd.name(), cmd.value(), Instant.now())),
                loaded.version());
        return variantId;
    }

    public void handle(RemoveVariantCommand cmd) {
        var loaded = loadActive(cmd.flagId());
        if (!loaded.flag().hasVariant(cmd.variantId())) {
            throw new VariantNotFoundException(cmd.flagId(), cmd.variantId());
        }
        eventStore.append(cmd.flagId(), List.of(
                new VariantRemovedEvent(cmd.flagId(), cmd.variantId(), Instant.now())),
                loaded.version());
    }

    public void handle(ModifyVariantCommand cmd) {
        var loaded = loadActive(cmd.flagId());
        if (!loaded.flag().hasVariant(cmd.variantId())) {
            throw new VariantNotFoundException(cmd.flagId(), cmd.variantId());
        }
        eventStore.append(cmd.flagId(), List.of(
                new VariantModifiedEvent(cmd.flagId(), cmd.variantId(), cmd.name(), cmd.value(), Instant.now())),
                loaded.version());
    }

    private record Loaded(FeatureFlag flag, long version) {}

    private Loaded loadActive(UUID flagId) {
        var stream = eventStore.load(flagId);
        if (stream.events().isEmpty()) throw new FlagNotFoundException(flagId);
        var flag = FeatureFlag.reconstitute(stream.events());
        if (flag.isDeleted()) throw new FlagDeletedException(flagId);
        return new Loaded(flag, stream.version());
    }
}
