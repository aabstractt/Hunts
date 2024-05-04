package it.bitrule.hunts.config;

/**
 * @param maxNameLength  The maximum characters of the faction name.
 * @param minNameLength  The minimum characters of the faction name.
 * @param maxMembers     The maximum members of the faction.
 * @param initialPower   The initial power of the faction.
 * @param powerIncrement The power increment of the faction.
 */
public record FactionsConfig(
        int maxNameLength,
        int minNameLength,
        int maxMembers,
        double initialPower,
        double powerIncrement
) {}