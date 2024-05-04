package it.bitrule.hunts.config;

import lombok.NonNull;

public record YamlConfig(
        @NonNull FactionsConfig factions
) {}