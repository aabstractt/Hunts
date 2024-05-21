package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.hunts.profile.Profile;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.hunts.controller.ProfileController;
import it.bitrule.plorex.commands.abstraction.argument.Argument;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.actor.CommandActor;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.NonNull;

public final class FactionSetHomeArgument extends Argument {

    public FactionSetHomeArgument() {
        super(
                "sethome",
                new String[0],
                ArgumentSpec.of(
                        "Usage: /<label> sethome",
                        Predicates.alwaysTrue()
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NonNull String[] args) {
        Player source = commandActor.toPlayer().orElse(null);
        if (source == null) {
            commandActor.sendMessage(TextFormat.RED + "This command can only be executed by players.");

            return;
        }

        Profile profile = ProfileController.getInstance().getProfileIfLoaded(source.getLoginChainData().getXUID());
        if (profile == null) {
            source.sendMessage(TextFormat.RED + "Your profile is not loaded.");

            return;
        }

        Faction faction = FactionController.getInstance().getFactionByPlayer(source);
        if (faction == null) {
            source.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        FactionMember selfFactionMember = faction.getMemberByXuid(source.getLoginChainData().getXUID());
        if (selfFactionMember == null) {
            source.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        if (!selfFactionMember.getRole().equals(FactionRole.LEADER)) {
            source.sendMessage(TextFormat.RED + "You must be the leader of the faction to set the home location.");

            return;
        }

        // TODO: Check if is outside of claim to prevent use that

        faction.getModel().setHq(source.getLocation());

        faction.broadcast(TranslationKey.FACTION_HQ_UPDATED.build(
                source.getName()
        ));
    }
}