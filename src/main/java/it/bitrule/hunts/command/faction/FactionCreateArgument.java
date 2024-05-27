package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import it.bitrule.hunts.Hunts;
import it.bitrule.hunts.Promise;
import it.bitrule.hunts.TranslationKey;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.hunts.profile.ProfileInfo;
import it.bitrule.hunts.controller.FactionController;
import it.bitrule.hunts.controller.ProfileController;
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

        String factionName = args[0].trim();
        if (factionName.isEmpty()) {
            commandActor.sendMessage(TextFormat.RED + "The faction name cannot be empty");

            return;
        }

        if (factionName.length() < Hunts.getYamlConfig().factions().minNameLength() || factionName.length() > Hunts.getYamlConfig().factions().maxNameLength()) {
            commandActor.sendMessage(TranslationKey.FACTION_NO_VALID_NAME.build());

            return;
        }

        ProfileInfo profileInfo = ProfileController.getInstance().getProfileIfLoaded(player.getLoginChainData().getXUID());
        if (profileInfo == null) {
            commandActor.sendMessage(TextFormat.RED + "Your profile is not loaded");

            return;
        }

        if (FactionController.getInstance().getFactionByName(factionName) != null) {
            commandActor.sendMessage(TranslationKey.FACTION_ALREADY_EXISTS.build(factionName));

            return;
        }

        if (FactionController.getInstance().getFactionByPlayer(player) != null) {
            commandActor.sendMessage(TranslationKey.PLAYER_SELF_ALREADY_IN_FACTION.build());

            return;
        }

        if (!factionName.matches("^[a-zA-Z0-9_]*$")) {
            commandActor.sendMessage(TextFormat.RED + "The faction name must contain only letters, numbers, and underscores");

            return;
        }

        Faction faction = Faction.empty(factionName);
        faction.addMember(FactionMember.create(profileInfo.getModel(), FactionRole.LEADER));

        FactionController.getInstance().cache(faction);
        FactionController.getInstance().cacheMember(player.getLoginChainData().getXUID(), faction.getConvertedId());

        ProfileController.getInstance().cacheXuid(player.getName(), player.getLoginChainData().getXUID());

        Promise.runAsync(() -> Hunts.getFactionRepository().save(faction.getModel()));

        Server.getInstance().broadcastMessage(TranslationKey.FACTION_SUCCESSFULLY_CREATED.build(factionName, player.getName()));
    }
}