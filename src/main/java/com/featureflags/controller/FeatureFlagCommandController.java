package com.featureflags.controller;

import com.featureflags.command.AddEnvironmentCommand;
import com.featureflags.command.AddVariantCommand;
import com.featureflags.command.CreateFeatureFlagCommand;
import com.featureflags.command.DeleteFeatureFlagCommand;
import com.featureflags.command.ModifyVariantCommand;
import com.featureflags.command.RemoveEnvironmentCommand;
import com.featureflags.command.RemoveVariantCommand;
import com.featureflags.command.RenameFeatureFlagCommand;
import com.featureflags.handler.FeatureFlagCommandHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/flags")
public class FeatureFlagCommandController {

    private final FeatureFlagCommandHandler handler;

    public FeatureFlagCommandController(FeatureFlagCommandHandler handler) {
        this.handler = handler;
    }

    private record CreateFlagBody(@NotBlank String name) {}

    private record RenameFlagBody(@NotBlank String name) {}

    private record AddEnvironmentBody(@NotBlank String name) {}

    private record AddVariantBody(@NotBlank String name, @NotBlank String value) {}

    private record ModifyVariantBody(@NotBlank String name, @NotBlank String value) {}

    @PostMapping
    public ResponseEntity<Map<String, UUID>> createFlag(@RequestBody @Valid CreateFlagBody body) {
        UUID flagId = handler.handle(new CreateFeatureFlagCommand(body.name()));
        return ResponseEntity.created(URI.create("/flags/" + flagId))
                .body(Map.of("flagId", flagId));
    }

    @DeleteMapping("/{flagId}")
    public ResponseEntity<Void> deleteFlag(@PathVariable UUID flagId) {
        handler.handle(new DeleteFeatureFlagCommand(flagId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{flagId}/name")
    public ResponseEntity<Void> renameFlag(@PathVariable UUID flagId, @RequestBody @Valid RenameFlagBody body) {
        handler.handle(new RenameFeatureFlagCommand(flagId, body.name()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{flagId}/environments")
    public ResponseEntity<Void> addEnvironment(@PathVariable UUID flagId, @RequestBody @Valid AddEnvironmentBody body) {
        handler.handle(new AddEnvironmentCommand(flagId, body.name()));
        return ResponseEntity.created(URI.create("/flags/" + flagId + "/environments/" + body.name())).build();
    }

    @DeleteMapping("/{flagId}/environments/{environmentName}")
    public ResponseEntity<Void> removeEnvironment(@PathVariable UUID flagId, @PathVariable String environmentName) {
        handler.handle(new RemoveEnvironmentCommand(flagId, environmentName));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{flagId}/variants")
    public ResponseEntity<Map<String, UUID>> addVariant(@PathVariable UUID flagId, @RequestBody @Valid AddVariantBody body) {
        UUID variantId = handler.handle(new AddVariantCommand(flagId, body.name(), body.value()));
        return ResponseEntity.created(URI.create("/flags/" + flagId + "/variants/" + variantId))
                .body(Map.of("variantId", variantId));
    }

    @DeleteMapping("/{flagId}/variants/{variantId}")
    public ResponseEntity<Void> removeVariant(@PathVariable UUID flagId, @PathVariable UUID variantId) {
        handler.handle(new RemoveVariantCommand(flagId, variantId));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{flagId}/variants/{variantId}")
    public ResponseEntity<Void> modifyVariant(
            @PathVariable UUID flagId,
            @PathVariable UUID variantId,
            @RequestBody @Valid ModifyVariantBody body) {
        handler.handle(new ModifyVariantCommand(flagId, variantId, body.name(), body.value()));
        return ResponseEntity.ok().build();
    }
}
