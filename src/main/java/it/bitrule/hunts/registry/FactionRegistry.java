package it.bitrule.hunts.registry;

import cn.nukkit.Player;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
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
        String sourceXuid = ProfileRegistry.getInstance().getPlayerXuid(sourceName);
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

    /**
     * Register a new faction.
     *
     * @param faction The faction to register.
     */
    public void registerNewFaction(@NonNull Faction faction) {
        this.factions.put(faction.getConvertedId(), faction);
        this.factionNames.put(faction.getModel().getName().toLowerCase(), faction.getConvertedId());
    }

    /**
     * Set the player's faction.
     *
     * @param factionMember The faction member.
     * @param faction The faction or null if the player is not in a faction.
     */
    public void setPlayerFaction(@NonNull FactionMember factionMember, @NonNull Faction faction) {
        ProfileRegistry.getInstance().setPlayerXuid(factionMember.getName(), factionMember.getXuid());

        this.playersFaction.put(factionMember.getXuid(), faction.getConvertedId());

        faction.addMember(factionMember);
    }

    /**
     * Clear the player's faction.
     *
     * @param factionMember The faction member.
     */
    public void clearPlayerFaction(@NonNull FactionMember factionMember) {
        ProfileRegistry.getInstance().removePlayerXuid(factionMember.getName());

        UUID factionId = this.playersFaction.remove(factionMember.getXuid());
        if (factionId == null) return;

        Faction faction = this.factions.get(factionId);
        if (faction == null) {
            throw new IllegalStateException("Faction does not exist");
        }

        //faction.removeMember(factionMember); // TODO: Add method to remove member from the faction
    }
}