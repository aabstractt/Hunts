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

public final class FactionWithdrawArgument extends Argument {

    public FactionWithdrawArgument() {
        super(
                "withdraw",
                new String[0],
                ArgumentSpec.of(
                        "Usage: /<label> withdraw <amount>",
                        Predicates.is(1),
                        ArgumentSpec.integer("amount", false)
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
            commandSender.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        Integer amount = Hunts.parseInteger(args[0]);
        if (amount == null || amount <= 0) {
            commandActor.sendMessage(TextFormat.RED + "Please provide a valid amount.");

            return;
        }

        FactionMember factionMember = faction.getMemberByXuid(commandSender.getLoginChainData().getXUID());
        if (factionMember == null) {
            commandSender.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        if (!factionMember.getRole().isAboveOrEqualsThan(FactionRole.OFFICER)) {
            commandSender.sendMessage(TextFormat.RED + "You must be an officer or the leader of the faction to withdraw money.");

            return;
        }

        if (faction.getModel().getBalance() < amount) {
            commandSender.sendMessage(TextFormat.RED + "Your faction does not have enough money.");

            return;
        }

        profile.getModel().setBalance(profile.getModel().getBalance() + amount);
        profile.setDirty();

        faction.getModel().setBalance(faction.getModel().getBalance() - amount);

        // TODO: Implement the withdraw message
        commandSender.sendMessage(TranslationKey.PLAYER_WITHDREW_MONEY.build(amount));
        faction.broadcast(TranslationKey.FACTION_WITHDREW_MONEY.build(commandSender.getName(), amount));

        FactionController.getInstance().markFactionDirty(faction);
    }
}