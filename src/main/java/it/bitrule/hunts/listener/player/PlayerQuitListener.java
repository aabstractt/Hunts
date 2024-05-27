package it.bitrule.hunts.listener.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import it.bitrule.hunts.Hunts;
import it.bitrule.hunts.Promise;
import it.bitrule.hunts.profile.ProfileInfo;
import it.bitrule.hunts.controller.ProfileController;
import lombok.NonNull;

public final class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuitEvent(@NonNull PlayerQuitEvent ev) {
        Player player = ev.getPlayer();

        ProfileInfo profileInfo = ProfileController.getInstance().removeProfile(player.getLoginChainData().getXUID());
        if (profileInfo == null) return;

        if (!profileInfo.isDirty()) return;

        Promise.runAsync(() -> Hunts.getProfileRepository().save(profileInfo.getModel()));

        profileInfo.notifySaved();
    }
}