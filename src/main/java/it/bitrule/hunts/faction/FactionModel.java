package it.bitrule.hunts.faction;

import cn.nukkit.level.Location;
import com.google.gson.annotations.SerializedName;
import it.bitrule.hunts.Hunts;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@RequiredArgsConstructor @Data
public final class FactionModel implements IModel {

    /**
     * The identifier of the faction.
     */
    @SerializedName("_id")
    private final @NonNull String identifier;
    /**
     * The name of the faction.
     */
    private final @NonNull String name;
    /**
     * The display name of the faction.
     */
    private @Nullable String displayName;
    /**
     * The HQ of the faction.
     */
    private @Nullable Location hq = null;
    /**
     * The DTR of the faction.
     */
    private double deathsUntilRaidable;
    /**
     * The points of the faction.
     */
    private int points;
    /**
     * The balance of the faction.
     */
    private int balance;
    /**
     * The koth captures of the faction.
     */
    private int kothCaptures;

    /**
     * The members of the faction.
     * The key is the member's identifier and the value is the member's role.
     */
    private final @NonNull Map<String, FactionRole> members = new HashMap<>();
    /**
     * The invites sent by the faction.
     */
    private final @NonNull Set<String> invitesSent = new HashSet<>();

    /**
     * Create a new faction model.
     *
     * @param name The name of the faction.
     * @return The faction model.
     */
    public static @NonNull FactionModel create(@NonNull String name) {
        FactionModel factionModel = new FactionModel(UUID.randomUUID().toString(), name);
        factionModel.setDeathsUntilRaidable(Hunts.getInstance().getConfig().getDouble("factions.default-dtr"));

        return factionModel;
    }
}