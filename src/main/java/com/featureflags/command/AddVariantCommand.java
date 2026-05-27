package com.featureflags.command;

import java.util.UUID;

public record AddVariantCommand(UUID flagId, String name, String value) {}
