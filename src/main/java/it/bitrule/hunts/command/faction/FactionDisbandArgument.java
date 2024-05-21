package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.Hunts;
import it.bitrule.hunts.Promise;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.hunts.controller.ProfileController;
import it.bitrule.plorex.commands.abstraction.argument.Argument;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.actor.CommandActor;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.NonNull;

public final class FactionDisbandArgument extends Argument {

    public FactionDisbandArgument() {
        super(
                "disband",
                new String[0],
                ArgumentSpec.of(
                        "/<label> disband",
                        Predicates.alwaysTrue()
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NonNull String[] args) {
        Player source = commandActor.toPlayer().orElse(null);
        if (source == null) {
            commandActor.sendMessage(TextFormat.RED + "Only players can kick players from factions");

            return;
        }

        Faction faction = FactionController.getInstance().getFactionByPlayer(source);
        if (faction == null) {
            source.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        FactionMember lazyFactionMember = faction.getMemberByXuid(source.getLoginChainData().getXUID());
        if (lazyFactionMember == null) {
            source.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        if (lazyFactionMember.getRole() != FactionRole.LEADER) {
            source.sendMessage(TextFormat.RED + "You must be the leader of the faction to kick players");

            return;
        }

        // Put the disband message in a variable to avoid repeating the same message
        String disbandMessage = TranslationKey.FACTION_SUCCESSFULLY_DISBANDED.build(faction.getModel().getName());

        for (FactionMember factionMember : faction.getFactionMembers()) {
            FactionController.getInstance().clearMember(factionMember.getXuid());
            ProfileController.getInstance().clearXuid(factionMember.getName());

            faction.removeMember(factionMember.getXuid());

            Player player = ProfileController.getInstance().getPlayerObject(factionMember.getXuid());
            if (player == null) continue;

            player.sendMessage(disbandMessage);
        }

        faction.getFactionMembers().clear();
        faction.getModel().getMembers().clear();

        // Delete the faction from the database
        Promise.runAsync(() -> Hunts.getFactionRepository().delete(faction.getConvertedId().toString()));
    }
}