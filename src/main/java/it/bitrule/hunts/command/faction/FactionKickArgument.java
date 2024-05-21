package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.controller.ProfileController;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.plorex.commands.abstraction.argument.Argument;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.actor.CommandActor;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.NonNull;

import java.util.Optional;

public final class FactionKickArgument extends Argument {

    public FactionKickArgument() {
        super(
                "kick",
                new String[0],
                ArgumentSpec.of(
                        "/<label> kick <player>",
                        Predicates.not(0),
                        ArgumentSpec.target("player")
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

        FactionMember selfFactionMember = faction.getMemberByXuid(source.getLoginChainData().getXUID());
        if (selfFactionMember == null) {
            source.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        if (selfFactionMember.getRole() != FactionRole.LEADER) {
            source.sendMessage(TextFormat.RED + "You must be the leader of the faction to kick players");

            return;
        }

        FactionMember factionMember = Optional.ofNullable(Server.getInstance().getPlayer(args[0]))
                .map(player -> faction.getMemberByXuid(player.getLoginChainData().getXUID()))
                .orElse(faction.getMemberByName(args[0]));
        if (factionMember == null) {
            source.sendMessage(TranslationKey.PLAYER_NOT_FACTION_MEMBER.build(args[0]));

            return;
        }

        if (factionMember.getXuid().equals(source.getLoginChainData().getXUID())) {
            source.sendMessage(TranslationKey.PLAYER_CANNOT_KICK_SELF.build());

            return;
        }

        String kickedSomeoneMessage = TranslationKey.FACTION_SUCCESSFULLY_KICKED_SOMEONE.build(source.getName(), factionMember.getName());
        for (FactionMember lazyFactionMember : faction.getFactionMembers()) {
            Player target = lazyFactionMember.wrapPlayer();
            if (target == null || !target.isOnline()) continue;

            target.sendMessage(kickedSomeoneMessage);
        }

        FactionController.getInstance().clearMember(factionMember.getXuid());
        ProfileController.getInstance().clearXuid(factionMember.getName());

        faction.removeMember(factionMember.getXuid());

        FactionController.getInstance().markFactionDirty(faction); // Mark the faction as dirty to save it

        Player target = factionMember.wrapPlayer();
        if (target == null || !target.isOnline()) return;

        target.sendMessage(TranslationKey.PLAYER_SELF_KICKED.build(source.getName()));
    }
}