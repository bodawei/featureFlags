package com.featureflags.command;

import java.util.UUID;

public record AddVariantCommand(UUID flagId, String name, String value) {
    public AddVariantCommand {
        CommandValidation.requireNonNull(flagId, "flagId");
        CommandValidation.requireNonBlank(name, "name");
        CommandValidation.requireNonBlank(value, "value");
    }
}
