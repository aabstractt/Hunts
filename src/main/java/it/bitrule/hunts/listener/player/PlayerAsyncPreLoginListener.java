package it.bitrule.hunts.listener.player;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerAsyncPreLoginEvent;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.Hunts;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.profile.Profile;
import it.bitrule.hunts.profile.ProfileModel;
import it.bitrule.hunts.controller.ProfileController;
import lombok.NonNull;

import java.util.Objects;

import static cn.nukkit.event.player.PlayerAsyncPreLoginEvent.*;

public final class PlayerAsyncPreLoginListener implements Listener {

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerAsyncPreLoginEvent(@NonNull PlayerAsyncPreLoginEvent ev) {
        if (!ev.getLoginResult().equals(LoginResult.SUCCESS)) return;

        String xuid = ev.getXuid().trim();
        if (xuid.isEmpty()) {
            ev.disAllow(TextFormat.RED + "XUID is empty");

            return;
        }

        ProfileModel profileModel = Hunts.getProfileRepository()
                .findOne(xuid)
                .orElseGet(() -> new ProfileModel(xuid));
        // TODO: Update the player's name and last name
        if (!Objects.equals(profileModel.getName(), ev.getName())) {
            profileModel.setLastName(profileModel.getName());
            profileModel.setName(ev.getName());

            Hunts.getProfileRepository().save(profileModel);
        }

        // Trigger the update member event
        ProfileController.getInstance().triggerUpdateMember(
                profileModel.getLastName(),
                ev.getName(),
                xuid
        );
        ProfileController.getInstance().registerNewProfile(new Profile(profileModel));

        // Trigger the update faction member event
        Faction faction = FactionController.getInstance().getFactionByPlayerXuid(xuid);
        if (faction == null) return;

        FactionMember factionMember = faction.getMemberByXuid(xuid);
        if (factionMember == null) return;

        factionMember.setName(ev.getName());
    }
}