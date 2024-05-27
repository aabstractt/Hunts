package it.bitrule.hunts.controller;

import cn.nukkit.Player;
import it.bitrule.hunts.profile.ProfileInfo;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ProfileController {

    @Getter private final static @NonNull ProfileController instance = new ProfileController();

    /**
     * The players' XUID.
     * The key is the player's name and the value is the player's XUID.
     */
    private final @NonNull Map<String, String> playersXuid = new ConcurrentHashMap<>();
    /**
     * The players' object.
     * The key is the player's xuid and the value is the player.
     */
    private final @NonNull Map<String, Player> playersObject = new ConcurrentHashMap<>();
    /**
     * The profiles loaded.
     * The key is the player's XUID and the value is the profile.
     */
    private final @NonNull Map<String, ProfileInfo> profiles = new ConcurrentHashMap<>();

    /**
     * Register a new profile.
     * @param profileInfo The profile to register.
     */
    public void registerNewProfile(@NonNull ProfileInfo profileInfo) {
        this.profiles.put(profileInfo.getModel().getIdentifier(), profileInfo);
    }

    /**
     * Get the profile of the player.
     * @param xuid The XUID of the player.
     * @return The profile of the player or null if the profile is not found.
     */
    public @Nullable ProfileInfo getProfileIfLoaded(@NonNull String xuid) {
        return this.profiles.get(xuid);
    }

    /**
     * Remove the profile of the player.
     *
     * @param xuid The XUID of the player.
     * @return The profile of the player or null if the profile is not found.
     */
    public @Nullable ProfileInfo removeProfile(@NonNull String xuid) {
        return this.profiles.remove(xuid);
    }

    /**
     * Set the player's XUID using the player's name.
     *
     * @param sourceName The name of the player.
     * @param sourceXuid The XUID of the player.
     */
    public void cacheXuid(@NonNull String sourceName, @NonNull String sourceXuid) {
        this.playersXuid.put(sourceName.toLowerCase(), sourceXuid);
    }

    /**
     * Get the player's XUID using the player's name.
     *
     * @param sourceName The name of the player.
     * @return The player's XUID or null if the player's XUID is not found.
     */
    public @Nullable String getPlayerXuid(@NonNull String sourceName) {
        return this.playersXuid.get(sourceName.toLowerCase());
    }

    /**
     * Remove the player's XUID using the player's name.
     *
     * @param sourceName The name of the player.
     */
    public void clearXuid(@NonNull String sourceName) {
        this.playersXuid.remove(sourceName.toLowerCase());
    }

    /**
     * Set the player's object.
     *
     * @param sourceXuid The XUID of the player.
     * @param source The player object.
     */
    public void setPlayerObject(@NonNull String sourceXuid, @NonNull Player source) {
        this.playersObject.put(sourceXuid, source);
    }

    /**
     * Get the player's object.
     *
     * @param sourceXuid The XUID of the player.
     * @return The player object or null if the player object is not found.
     */
    public @Nullable Player getPlayerObject(@NonNull String sourceXuid) {
        return this.playersObject.get(sourceXuid);
    }

    /**
     * Trigger an update member event.
     *
     * @param sourceName the name of the source
     * @param sourceXuid the xuid of the source
     */
    public void triggerUpdateMember(@Nullable String oldSourceName, @NonNull String sourceName, @NonNull String sourceXuid) {
        if (oldSourceName == null || this.getPlayerXuid(oldSourceName) == null) return;
        // We only need to update the player's XUID if the player has faction
        // So if the xuid is null on the cache is because their don't have a faction
        // Because when use /f create or /f join the player's xuid is set
        // Or when the server load the factions

        this.clearXuid(oldSourceName);
        this.cacheXuid(sourceName, sourceXuid);
    }
}