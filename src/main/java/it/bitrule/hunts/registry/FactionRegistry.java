package it.bitrule.hunts.registry;

import cn.nukkit.Player;
import it.bitrule.hunts.faction.Faction;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FactionRegistry {

    @Getter private final static @NonNull FactionRegistry instance = new FactionRegistry();

    /**
     * The factions.
     * The key is the faction's identifier and the value is the faction.
     */
    private final @NonNull Map<UUID, Faction> factions = new ConcurrentHashMap<>();
    /**
     * The factions' name.
     * The key is the faction's name and the value is the faction's identifier.
     */
    private final @NonNull Map<String, UUID> factionNames = new ConcurrentHashMap<>();

    /**
     * The players' faction.
     * The key is the player's XUID and the value is the faction's identifier.
     */
    private final @NonNull Map<String, UUID> playersFaction = new ConcurrentHashMap<>();
    /**
     * The players' XUID.
     * The key is the player's name and the value is the player's XUID.
     */
    private final @NonNull Map<String, String> playersXuid = new ConcurrentHashMap<>();

    public void loadAll() {
        // Load all factions from the database
    }

    /**
     * Get the player's faction using the player's name.
     *
     * @param sourceName The name of the player.
     * @return The player's faction or null if the player is not in a faction.
     */
    public @Nullable Faction getFactionByPlayer(@NonNull String sourceName) {
        String sourceXuid = this.playersXuid.get(sourceName);
        if (sourceXuid == null) return null;

        UUID factionId = this.playersFaction.get(sourceXuid);
        if (factionId == null) return null;

        return this.factions.get(factionId);
    }

    /**
     * Get the player's faction using their object.
     * Usually used when the player is online.
     *
     * @param source The player object.
     * @return The player's faction or null if the player is not in a faction.
     */
    public @Nullable Faction getFactionByPlayer(@NonNull Player source) {
        UUID factionId = this.playersFaction.get(source.getLoginChainData().getXUID());
        if (factionId == null) return null;

        return this.factions.get(factionId);
    }

    /**
     * Get a faction by its name.
     *
     * @param name The name of the faction.
     * @return The faction or null if the faction does not exist.
     */
    public @Nullable Faction getFactionByName(@NonNull String name) {
        UUID factionId = this.factionNames.get(name.toLowerCase());
        if (factionId == null) return null;

        return this.factions.get(factionId);
    }
}