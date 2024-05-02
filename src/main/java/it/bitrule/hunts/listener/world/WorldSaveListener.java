package it.bitrule.hunts.listener.world;

import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.LevelSaveEvent;
import it.bitrule.hunts.Promise;
import it.bitrule.hunts.registry.FactionRegistry;
import lombok.NonNull;

public final class WorldSaveListener implements Listener {

    @EventHandler
    public void onWorldSaveEvent(@NonNull LevelSaveEvent ev) {
        if (!ev.getLevel().equals(Server.getInstance().getDefaultLevel())) return;

        Promise.runAsync(() -> FactionRegistry.getInstance().saveAll(false));
    }
}