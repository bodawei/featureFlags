package com.featureflags.command;

import java.util.UUID;

public record AddEnvironmentCommand(UUID flagId, String environmentName) {}
