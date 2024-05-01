package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.hunts.registry.FactionRegistry;
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

        Faction faction = FactionRegistry.getInstance().getFactionByPlayer(source);
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

        FactionMember factionMember = Optional.ofNullable(Server.getInstance().getPlayer(args[0]))
                .map(player -> faction.getMemberByXuid(player.getLoginChainData().getXUID()))
                .orElse(faction.getMemberByName(args[0]));
        if (factionMember == null) {
            source.sendMessage(TranslationKey.PLAYER_NOT_FACTION_MEMBER.build(args[0]));

            return;
        }

        if (factionMember.getXuid().equals(source.getLoginChainData().getXUID())) {
            source.sendMessage(TextFormat.DARK_RED + "You cannot kick yourself from the faction");

            return;
        }

        FactionRegistry.getInstance().clearPlayerFaction(factionMember);

        // TODO: Send a message to the kicked player
        // TODO: Send a message to the faction members
    }

}