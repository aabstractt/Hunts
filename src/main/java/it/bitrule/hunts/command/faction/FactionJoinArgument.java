package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.Hunts;
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
import org.jetbrains.annotations.NotNull;

public final class FactionJoinArgument extends Argument {

    public FactionJoinArgument() {
        super(
                "join",
                new String[] {"accept"},
                ArgumentSpec.of(
                        "Usage: /<label> join <faction>",
                        Predicates.not(0),
                        ArgumentSpec.string(
                                "faction",
                                FactionController.getInstance()
                                        .getAll().stream()
                                        .map(faction -> faction.getModel().getName().toLowerCase())
                                        .toList().toArray(new String[0])
                        )
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NotNull @NonNull String[] args) {
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

        if (FactionController.getInstance().getFactionByPlayer(commandSender) != null) {
            commandSender.sendMessage(TranslationKey.PLAYER_SELF_ALREADY_IN_FACTION.build());

            return;
        }

        Faction faction = FactionController.getInstance().getFactionByName(args[0]);
        if (faction == null) {
            commandSender.sendMessage(TranslationKey.FACTION_NOT_FOUND.build(args[0]));

            return;
        }

        if (faction.getMemberByXuid(commandSender.getLoginChainData().getXUID()) != null) {
            commandActor.sendMessage(TranslationKey.PLAYER_SELF_ALREADY_IN_FACTION.build());

            return;
        }

        if (!faction.getModel().getInvitesSent().contains(commandSender.getLoginChainData().getXUID())) {
            commandSender.sendMessage(TranslationKey.FACTION_SELF_NOT_INVITED.build(faction.getModel().getName()));

            return;
        }

        if (faction.isFull()) {
            commandSender.sendMessage(TextFormat.RED + "This faction is full.");

            return;
        }

        faction.addMember(FactionMember.create(
                profile.getModel(),
                FactionRole.MEMBER
        ));
        FactionController.getInstance().markFactionDirty(faction);

        FactionController.getInstance().cacheMember(
                commandSender.getLoginChainData().getXUID(),
                faction.getConvertedId()
        );
        ProfileController.getInstance().cacheXuid(
                commandSender.getName(),
                commandSender.getLoginChainData().getXUID()
        );

        faction.setRemainingRegenerationTime(Hunts.getYamlConfig().factions().powerFreeze() * (60 * 1000L));
        faction.getModel().getInvitesSent().remove(commandSender.getLoginChainData().getXUID());

        faction.broadcast(TranslationKey.FACTION_MEMBER_SUCCESSFULLY_JOINED.build(
                commandSender.getName()
        ));
        commandSender.sendMessage(TranslationKey.PLAYER_SELF_JOINED_FACTION.build(
                faction.getModel().getName()
        ));
    }
}