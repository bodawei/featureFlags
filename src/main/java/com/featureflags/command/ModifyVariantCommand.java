package com.featureflags.command;

import java.util.UUID;

public record ModifyVariantCommand(UUID flagId, UUID variantId, String name, String value) {}
