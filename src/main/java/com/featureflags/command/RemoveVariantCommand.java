package com.featureflags.command;

import java.util.UUID;

public record RemoveVariantCommand(UUID flagId, UUID variantId) {
    public RemoveVariantCommand {
        CommandValidation.requireNonNull(flagId, "flagId");
        CommandValidation.requireNonNull(variantId, "variantId");
    }
}
