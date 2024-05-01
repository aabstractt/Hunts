package it.bitrule.hunts.faction.member;

import cn.nukkit.Player;
import it.bitrule.hunts.profile.ProfileModel;
import it.bitrule.hunts.registry.ProfileRegistry;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@AllArgsConstructor @Data
public final class FactionMember {

    /**
     * The XUID of the player.
     */
    private final @NonNull String xuid;
    /**
     * The role of the player.
     */
    private @NonNull FactionRole role;

    /**
     * The name of the player.
     */
    private @NonNull String name;
    /**
     * The kills of the player.
     */
    private int kills;
    /**
     * The deaths of the player.
     */
    private int deaths;

    /**
     * Get the player object.
     *
     * @return The player object or null if the player is not online.
     */
    public @Nullable Player wrapPlayer() {
        return ProfileRegistry.getInstance().getPlayerObject(this.xuid);
    }

    /**
     * Create a new faction member from a profile.
     * @param profileModel The profile model.
     * @param factionRole The faction role.
     * @return The faction member.
     */
    public static @NonNull FactionMember create(@NonNull ProfileModel profileModel, @NonNull FactionRole factionRole) {
        return new FactionMember(
                profileModel.getIdentifier(),
                factionRole,
                Objects.requireNonNull(profileModel.getName()),
                profileModel.getKills(),
                profileModel.getDeaths()
        );
    }
}