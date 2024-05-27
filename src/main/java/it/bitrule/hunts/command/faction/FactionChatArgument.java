package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.hunts.controller.ProfileController;
import it.bitrule.hunts.profile.ProfileInfo;
import it.bitrule.plorex.commands.abstraction.argument.Argument;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.actor.CommandActor;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.NonNull;

public final class FactionChatArgument extends Argument {

    public FactionChatArgument() {
        super(
                "chat",
                new String[] {"c"},
                ArgumentSpec.of(
                        "Usage: /<label> chat",
                        Predicates.alwaysTrue()
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NonNull String[] strings) {
        Player commandSender = commandActor.toPlayer().orElse(null);
        if (commandSender == null) {
            commandActor.sendMessage(TextFormat.RED + "This command can only be executed by players.");

            return;
        }

        ProfileInfo profileInfo = ProfileController.getInstance().getProfileIfLoaded(commandSender.getLoginChainData().getXUID());
        if (profileInfo == null) {
            commandSender.sendMessage(TextFormat.RED + "Your profile is not loaded.");

            return;
        }

        if (FactionController.getInstance().getFactionByPlayer(commandSender) == null) {
            commandSender.sendMessage(TranslationKey.PLAYER_SELF_MUST_BE_IN_FACTION.build());

            return;
        }

        profileInfo.setFactionChat(!profileInfo.isFactionChat());

        // TODO: Implement this message to the TranslationKey class
        commandSender.sendMessage(TextFormat.YELLOW + "Faction chat is now " + (profileInfo.isFactionChat() ? TextFormat.GREEN + "enabled" : TextFormat.RED + "disabled") + TextFormat.YELLOW + ".");
    }
}