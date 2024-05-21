package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.plorex.commands.abstraction.argument.Argument;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.actor.CommandActor;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.NonNull;

import java.util.Optional;

public final class FactionTransferArgument extends Argument {

    public FactionTransferArgument() {
        super(
                "transfer",
                new String[0],
                ArgumentSpec.of(
                        "Usage: /<label> transfer <player>",
                        Predicates.not(0),
                        ArgumentSpec.target("player")
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NonNull String[] args) {
        Player sender = commandActor.toPlayer().orElse(null);
        if (sender == null) {
            commandActor.sendMessage(TextFormat.RED + "This command can only be executed by players.");

            return;
        }

        Faction faction = FactionController.getInstance().getFactionByPlayer(sender);
        if (faction == null) {
            sender.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        FactionMember factionMember = faction.getMemberByXuid(sender.getLoginChainData().getXUID());
        if (factionMember == null) {
            sender.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        if (factionMember.getRole() != FactionRole.LEADER) {
            sender.sendMessage(TextFormat.RED + "You must be the leader of the faction to transfer leadership");

            return;
        }

        // LazyFactionMember is a better name for this variable
        // I mean to the target player, not the sender
        FactionMember lazyFactionMember = Optional.ofNullable(Server.getInstance().getPlayer(args[0]))
                .map(player -> faction.getMemberByXuid(player.getLoginChainData().getXUID()))
                .orElseGet(() -> faction.getMemberByName(args[0]));
        if (lazyFactionMember == null) {
            sender.sendMessage(TranslationKey.PLAYER_NOT_FACTION_MEMBER.build(args[0]));

            return;
        }

        factionMember.setRole(FactionRole.OFFICER);
        lazyFactionMember.setRole(FactionRole.LEADER);

        FactionController.getInstance().markFactionDirty(faction);

        faction.broadcast(TextFormat.colorize("&9" + sender.getName() + "&e has transferred leadership to &6" + lazyFactionMember.getName() + "&e."));
    }
}