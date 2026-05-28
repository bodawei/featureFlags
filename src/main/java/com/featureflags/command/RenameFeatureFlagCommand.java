package com.featureflags.command;

import java.util.UUID;

public record RenameFeatureFlagCommand(UUID flagId, String newName) {}
