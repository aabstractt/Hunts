package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.profile.Profile;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.hunts.controller.ProfileController;
import it.bitrule.plorex.commands.abstraction.argument.Argument;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.actor.CommandActor;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.NonNull;
import org.apache.commons.lang3.math.NumberUtils;

public final class FactionDepositArgument extends Argument {

    public FactionDepositArgument() {
        super(
                "deposit",
                new String[]{"d"},
                ArgumentSpec.of(
                        "Usage: /<label> deposit <all|amount>",
                        Predicates.is(1)
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NonNull String[] args) {
        Player source = commandActor.toPlayer().orElse(null);
        if (source == null) {
            source.sendMessage(TextFormat.RED + "This command can only be executed by players.");

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

        Integer amount = NumberUtils.isNumber(args[0]) ? Integer.parseInt(args[0]) : null;
        if (amount == null && args[0].equalsIgnoreCase("all")) {
            amount = profile.getModel().getBalance();
        }

        if (amount == null || amount <= 0) {
            source.sendMessage(TextFormat.RED + "Please provide a valid amount.");

            return;
        }

        if (profile.getModel().getBalance() < amount) {
            source.sendMessage(TranslationKey.PLAYER_NOT_ENOUGH_BALANCE.build(profile.getModel().getBalance(), amount));

            return;
        }

        faction.getModel().setBalance(faction.getModel().getBalance() + amount);
        FactionController.getInstance().markFactionDirty(faction);

        profile.getModel().setBalance(profile.getModel().getBalance() - amount);
        profile.setDirty();

        source.sendMessage(TranslationKey.PLAYER_SELF_DEPOSITED_MONEY.build(amount));
        faction.broadcast(TranslationKey.FACTION_MEMBER_DEPOSITED_MONEY.build(source.getName(), amount));
    }
}