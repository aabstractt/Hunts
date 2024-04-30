package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.Hunts;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.FactionModel;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.hunts.profile.Profile;
import it.bitrule.hunts.registry.FactionRegistry;
import it.bitrule.hunts.registry.ProfileRegistry;
import it.bitrule.plorex.commands.abstraction.argument.Argument;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.actor.CommandActor;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.NonNull;

public final class FactionCreateArgument extends Argument {

    public FactionCreateArgument() {
        super(
                "create",
                new String[0],
                ArgumentSpec.of(
                        "/<label> create <name>",
                        Predicates.not(0),
                        ArgumentSpec.string("name")
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NonNull String[] args) {
        Player player = commandActor.toPlayer().orElse(null);
        if (player == null) {
            commandActor.sendMessage(TextFormat.RED + "Only players can create factions");

            return;
        }

        Profile profile = ProfileRegistry.getInstance().getProfileIfLoaded(player.getLoginChainData().getXUID());
        if (profile == null) {
            commandActor.sendMessage(TextFormat.RED + "Your profile is not loaded");

            return;
        }

        if (FactionRegistry.getInstance().getFactionByName(args[0]) != null) {
            commandActor.sendMessage(TextFormat.RED + "Faction already exists"); // TODO: Add message to the locale

            return;
        }

        if (FactionRegistry.getInstance().getFactionByPlayer(player) != null) {
            commandActor.sendMessage(TextFormat.RED + "You are already in a faction"); // TODO: Add message to the locale

            return;
        }

        if (args[0].length() < 3 || args[0].length() > 16) {
            commandActor.sendMessage(TextFormat.RED + "The faction name must be between 3 and 16 characters"); // TODO: Add message to the locale

            return;
        }

        if (!args[0].matches("^[a-zA-Z0-9_]*$")) {
            commandActor.sendMessage(TextFormat.RED + "The faction name must contain only letters, numbers, and underscores"); // TODO: Add message to the locale

            return;
        }

        // TODO: Create the faction

        Faction faction = Faction.from(FactionModel.create(args[0]));
        FactionRegistry.getInstance().setPlayerFaction(
                FactionMember.create(profile.getModel(), FactionRole.LEADER),
                faction
        );
        FactionRegistry.getInstance().registerNewFaction(faction);

        // TODO: Add the faction to the registry

        // TODO: Add promise class methods to save asynchronously
        Hunts.getFactionRepository().save(faction.getModel());
    }
}