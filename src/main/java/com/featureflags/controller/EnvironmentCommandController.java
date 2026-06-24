package com.featureflags.controller;

import com.featureflags.command.CreateEnvironmentCommand;
import com.featureflags.command.DeleteEnvironmentCommand;
import com.featureflags.command.UpdateEnvironmentCommand;
import com.featureflags.handler.EnvironmentCommandHandler;
import com.featureflags.handler.FlagReference;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/environments")
public class EnvironmentCommandController {

  private final EnvironmentCommandHandler handler;

  public EnvironmentCommandController(EnvironmentCommandHandler handler) {
    this.handler = handler;
  }

  private record CreateEnvironmentBody(@NotBlank String name, String description) {}

  private record UpdateEnvironmentBody(String name, String description) {}

  @PostMapping
  public ResponseEntity<Map<String, UUID>> createEnvironment(
      @RequestBody @Valid CreateEnvironmentBody body) {
    UUID environmentId =
        handler.handle(new CreateEnvironmentCommand(body.name(), body.description()));
    return ResponseEntity.created(URI.create("/environments/" + environmentId))
        .body(Map.of("environmentId", environmentId));
  }

  @PatchMapping("/{environmentId}")
  public ResponseEntity<Void> updateEnvironment(
      @PathVariable UUID environmentId, @RequestBody UpdateEnvironmentBody body) {
    handler.handle(
        new UpdateEnvironmentCommand(environmentId, body.name(), body.description()));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{environmentId}/usage")
  public ResponseEntity<Map<String, Object>> usage(@PathVariable UUID environmentId) {
    List<FlagReference> flags = handler.usage(environmentId);
    return ResponseEntity.ok(Map.of("environmentId", environmentId, "flags", flags));
  }

  @DeleteMapping("/{environmentId}")
  public ResponseEntity<Void> deleteEnvironment(@PathVariable UUID environmentId) {
    handler.handle(new DeleteEnvironmentCommand(environmentId));
    return ResponseEntity.noContent().build();
  }
}
