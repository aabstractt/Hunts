package it.bitrule.hunts.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor @Data
public final class FactionsConfig {

    /**
     * The maximum characters of the faction name.
     */
    private final int maxNameLength;
    /**
     * The minimum characters of the faction name.
     */
    private final int minNameLength;
    /**
     * The maximum members of the faction.
     */
    private final int maxMembers;

    /**
     * The initial power of the faction.
     */
    private final double initialPower;
    /**
     * The power increment of the faction.
     */
    private final double powerIncrement;
}