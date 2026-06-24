package com.featureflags.controller;

import com.featureflags.command.CreateVariantCommand;
import com.featureflags.command.DeleteVariantCommand;
import com.featureflags.command.UpdateVariantCommand;
import com.featureflags.handler.FlagReference;
import com.featureflags.handler.VariantCommandHandler;
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
@RequestMapping("/variants")
public class VariantCommandController {

  private final VariantCommandHandler handler;

  public VariantCommandController(VariantCommandHandler handler) {
    this.handler = handler;
  }

  private record CreateVariantBody(@NotBlank String value) {}

  private record UpdateVariantBody(String value) {}

  @PostMapping
  public ResponseEntity<Map<String, UUID>> createVariant(
      @RequestBody @Valid CreateVariantBody body) {
    UUID variantId = handler.handle(new CreateVariantCommand(body.value()));
    return ResponseEntity.created(URI.create("/variants/" + variantId))
        .body(Map.of("variantId", variantId));
  }

  @PatchMapping("/{variantId}")
  public ResponseEntity<Void> updateVariant(
      @PathVariable UUID variantId, @RequestBody UpdateVariantBody body) {
    handler.handle(new UpdateVariantCommand(variantId, body.value()));
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{variantId}/usage")
  public ResponseEntity<Map<String, Object>> usage(@PathVariable UUID variantId) {
    List<FlagReference> flags = handler.usage(variantId);
    return ResponseEntity.ok(Map.of("variantId", variantId, "flags", flags));
  }

  @DeleteMapping("/{variantId}")
  public ResponseEntity<Void> deleteVariant(@PathVariable UUID variantId) {
    handler.handle(new DeleteVariantCommand(variantId));
    return ResponseEntity.noContent().build();
  }
}
