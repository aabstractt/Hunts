package it.bitrule.hunts.listener.player;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import it.bitrule.hunts.controller.ProfileController;
import lombok.NonNull;

public final class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoinEvent(@NonNull PlayerJoinEvent ev) {
        Player player = ev.getPlayer();
        if (!player.isOnline()) {
            throw new IllegalStateException("The player is not online");
        }

        ProfileController.getInstance().setPlayerObject(
                player.getLoginChainData().getXUID(),
                player
        );
    }
}