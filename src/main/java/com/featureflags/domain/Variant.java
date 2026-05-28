package com.featureflags.domain;

import java.util.UUID;

public record Variant(UUID id, String name, String value) {}
