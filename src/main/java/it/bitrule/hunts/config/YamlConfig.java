package it.bitrule.hunts.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@AllArgsConstructor @Data
public final class YamlConfig {

    private final @NonNull FactionsConfig factions;
}