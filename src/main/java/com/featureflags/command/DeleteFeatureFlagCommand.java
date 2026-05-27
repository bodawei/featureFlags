package com.featureflags.command;

import java.util.UUID;

public record DeleteFeatureFlagCommand(UUID flagId) {}
