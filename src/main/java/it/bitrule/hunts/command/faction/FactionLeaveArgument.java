package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.hunts.controller.ProfileController;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.hunts.profile.Profile;
import it.bitrule.plorex.commands.abstraction.argument.Argument;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.actor.CommandActor;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.NonNull;

public final class FactionLeaveArgument extends Argument {

    public FactionLeaveArgument() {
        super(
                "leave",
                new String[0],
                ArgumentSpec.of(
                        "Usage: /<label> leave",
                        Predicates.alwaysTrue()
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NonNull String[] args) {
        Player commandSender = commandActor.toPlayer().orElse(null);
        if (commandSender == null) {
            commandActor.sendMessage(TextFormat.RED + "This command can only be executed by players.");

            return;
        }

        Profile profile = ProfileController.getInstance().getProfileIfLoaded(commandSender.getLoginChainData().getXUID());
        if (profile == null) {
            commandSender.sendMessage(TextFormat.RED + "Your profile is not loaded.");

            return;
        }

        Faction faction = FactionController.getInstance().getFactionByPlayer(commandSender);
        if (faction == null) {
            commandActor.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        FactionMember factionMember = faction.getMemberByXuid(commandSender.getLoginChainData().getXUID());
        if (factionMember == null) {
            commandSender.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        if (factionMember.getRole().equals(FactionRole.LEADER) && !faction.isSystem()) {
            commandSender.sendMessage(TextFormat.RED + "Please use /" + s + " disband to disband your faction.");

            return;
        }

        faction.broadcast(TranslationKey.FACTION_MEMBER_SUCCESSFULLY_LEAVED.build(
                commandSender.getName()
        ));
        commandSender.sendMessage(TranslationKey.PLAYER_SELF_LEAVED_FACTION.build(
                faction.getModel().getName()
        ));

        FactionController.getInstance().clearMember(commandSender.getLoginChainData().getXUID());
        ProfileController.getInstance().clearXuid(commandActor.getName());

        faction.removeMember(commandSender.getLoginChainData().getXUID());

        commandSender.sendData(commandSender.getViewers().values().toArray(new Player[0]));
        commandSender.getViewers().values().forEach(viewer -> viewer.sendData(commandSender));
    }
}