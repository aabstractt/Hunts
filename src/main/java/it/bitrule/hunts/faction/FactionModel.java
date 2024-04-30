package it.bitrule.hunts.faction;

import cn.nukkit.level.Location;
import com.google.gson.annotations.SerializedName;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
}