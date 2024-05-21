package it.bitrule.hunts.config;

import cn.nukkit.utils.ConfigSection;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

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
) {

    /**
     * Wrap the main section to a FactionsConfig object.
     *
     * @param mainSection The main section.
     * @return The FactionsConfig object.
     */
    public static @NonNull FactionsConfig wrap(@Nullable ConfigSection mainSection) {
        if (mainSection == null) {
            throw new IllegalStateException("Section for 'factions' is not found");
        }

        return new FactionsConfig(
                mainSection.getInt("max-name-length", 16),
                mainSection.getInt("min-name-length", 3),
                mainSection.getInt("max-members", 10),
                mainSection.getDouble("initial-power", 10.0),
                mainSection.getDouble("power-increment", 1.0)
        );
    }
}