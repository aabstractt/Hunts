package it.bitrule.hunts.listener.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import it.bitrule.hunts.Hunts;
import it.bitrule.hunts.Promise;
import it.bitrule.hunts.profile.Profile;
import it.bitrule.hunts.registry.ProfileRegistry;
import lombok.NonNull;

public final class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuitEvent(@NonNull PlayerQuitEvent ev) {
        Player player = ev.getPlayer();

        Profile profile = ProfileRegistry.getInstance().removeProfile(player.getLoginChainData().getXUID());
        if (profile == null) return;

        if (profile.isDirty()) {
            Promise.runAsync(() -> Hunts.getProfileRepository().save(profile.getModel()));

            profile.notifySaved();
        }
    }
}