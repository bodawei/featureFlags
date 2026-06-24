package com.featureflags.controller;

import com.featureflags.command.CreateFeatureFlagCommand;
import com.featureflags.command.DeleteFeatureFlagCommand;
import com.featureflags.command.UpdateFeatureFlagCommand;
import com.featureflags.handler.FeatureFlagCommandHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flags")
public class FeatureFlagCommandController {

  private final FeatureFlagCommandHandler handler;

  public FeatureFlagCommandController(FeatureFlagCommandHandler handler) {
    this.handler = handler;
  }

  private record CreateFlagBody(
      @NotBlank String name, Set<UUID> environmentIds, Set<UUID> variantIds) {}

  private record UpdateFlagBody(String name, Set<UUID> environmentIds, Set<UUID> variantIds) {}

  @PostMapping
  public ResponseEntity<Map<String, UUID>> createFlag(@RequestBody @Valid CreateFlagBody body) {
    UUID flagId =
        handler.handle(
            new CreateFeatureFlagCommand(body.name(), body.environmentIds(), body.variantIds()));
    return ResponseEntity.created(URI.create("/flags/" + flagId)).body(Map.of("flagId", flagId));
  }

  @PatchMapping("/{flagId}")
  public ResponseEntity<Void> updateFlag(
      @PathVariable UUID flagId, @RequestBody UpdateFlagBody body) {
    handler.handle(
        new UpdateFeatureFlagCommand(
            flagId, body.name(), body.environmentIds(), body.variantIds()));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{flagId}")
  public ResponseEntity<Void> deleteFlag(@PathVariable UUID flagId) {
    handler.handle(new DeleteFeatureFlagCommand(flagId));
    return ResponseEntity.noContent().build();
  }
}
