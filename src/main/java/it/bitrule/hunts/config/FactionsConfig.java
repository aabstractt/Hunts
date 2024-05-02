package it.bitrule.hunts.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor @Data
public final class FactionsConfig {

    private final int maxFactionNameLength;
    private final int minFactionNameLength;

    private final double initialPower;
    private final double powerIncrement;
}