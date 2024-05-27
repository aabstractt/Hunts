package it.bitrule.hunts.command.faction;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class FactionPromoteArgument extends Argument {

    public FactionPromoteArgument() {
        super(
                "promote",
                new String[0],
                ArgumentSpec.of(
                        "Usage: /<label> promote <player>",
                        Predicates.is(1),
                        ArgumentSpec.target("player")
                ),
                null
        );
    }

    @Override
    public void execute(@NonNull CommandActor commandActor, @NonNull String s, @NotNull @NonNull String[] args) {
        Player source = commandActor.toPlayer().orElse(null);
        if (source == null) {
            commandActor.sendMessage(TextFormat.RED + "This command can only be executed by players.");

            return;
        }

        ProfileInfo profileInfo = ProfileController.getInstance().getProfileIfLoaded(source.getLoginChainData().getXUID());
        if (profileInfo == null) {
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

        if (!selfFactionMember.getRole().equals(FactionRole.LEADER)) {
            source.sendMessage(TextFormat.RED + "You must be the leader of the faction to demote players");

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
            source.sendMessage(TextFormat.RED + "You can't demote yourself.");

            return;
        }

        if (factionMember.getRole().equals(FactionRole.MEMBER)) {
            source.sendMessage(TextFormat.RED + "You can't promote this player.");

            return;
        }

        if (factionMember.getRole().isAboveOrEqualsThan(FactionRole.OFFICER)) {
            source.sendMessage(TextFormat.RED + "You can't promote this player.");

            return;
        }

        factionMember.setRole(FactionRole.OFFICER);
        faction.broadcast(TranslationKey.FACTION_MEMBER_PROMOTED.build(
                factionMember.getName(),
                source.getName(),
                StringUtils.capitalize(FactionRole.OFFICER.name().toLowerCase())
        ));

        FactionController.getInstance().markFactionDirty(faction);

        Player target = factionMember.wrapPlayer();
        if (target == null || !target.isOnline()) return;

        target.sendMessage(TranslationKey.PLAYER_SELF_PROMOTED.build(
                source.getName(),
                StringUtils.capitalize(FactionRole.OFFICER.name().toLowerCase())
        ));
    }
}