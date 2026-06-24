package com.featureflags.command;

import java.util.UUID;

public record RemoveEnvironmentCommand(UUID flagId, String environmentName) {
    public RemoveEnvironmentCommand {
        CommandValidation.requireNonNull(flagId, "flagId");
        CommandValidation.requireNonBlank(environmentName, "environmentName");
    }
}
