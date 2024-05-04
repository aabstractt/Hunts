package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.Hunts;
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

public final class FactionInviteArgument extends Argument {

    public FactionInviteArgument() {
        super(
                "invite",
                new String[0],
                ArgumentSpec.of(
                        "/<label> invite <player>",
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

        FactionMember selfFactionMember = faction.getMemberByXuid(source.getLoginChainData().getXUID());
        if (selfFactionMember == null) {
            source.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        if (!selfFactionMember.getRole().isAboveOrEqualsThan(FactionRole.OFFICER)) {
            source.sendMessage(TextFormat.RED + "You must be an officer or the leader of the faction to invite players");

            return;
        }

        Player target = Server.getInstance().getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            source.sendMessage(TranslationKey.PLAYER_NOT_ONLINE.build(args[0]));

            return;
        }

        if (target.getLoginChainData().getXUID().equalsIgnoreCase(source.getLoginChainData().getXUID())) {
            source.sendMessage(TranslationKey.PLAYER_CANNOT_INVITE_SELF.build());

            return;
        }

        if (faction.getFactionMembers().size() >= Hunts.getYamlConfig().factions().maxMembers()) {
            source.sendMessage(TranslationKey.FACTION_FULL.build());

            return;
        }

        if (FactionRegistry.getInstance().getFactionByPlayer(target) != null) {
            source.sendMessage(TranslationKey.PLAYER_ALREADY_IN_FACTION.build(target.getName()));

            return;
        }

        if (faction.getModel().getInvitesSent().contains(target.getLoginChainData().getXUID())) {
            source.sendMessage(TranslationKey.FACTION_MEMBER_ALREADY_INVITED.build(target.getName()));

            return;
        }

        target.sendMessage(TranslationKey.FACTION_INVITE_RECEIVED.build(faction.getModel().getName(), source.getName()));
        source.sendMessage(TranslationKey.FACTION_INVITE_SENT.build(target.getName()));

        faction.broadcast(TranslationKey.FACTION_INVITE_BROADCAST.build(target.getName(), source.getName()));

        faction.getModel().getInvitesSent().add(target.getLoginChainData().getXUID());
        FactionRegistry.getInstance().markFactionDirty(faction);
    }
}