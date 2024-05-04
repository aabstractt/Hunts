package it.bitrule.hunts.registry;

import cn.nukkit.Player;
import it.bitrule.hunts.Hunts;
import it.bitrule.hunts.command.faction.*;
import it.bitrule.hunts.faction.Faction;
import it.bitrule.hunts.faction.FactionModel;
import it.bitrule.hunts.faction.member.FactionMember;
import it.bitrule.hunts.faction.member.FactionRole;
import it.bitrule.hunts.profile.ProfileModel;
import it.bitrule.plorex.commands.abstraction.MainCommand;
import it.bitrule.plorex.commands.abstraction.argument.spec.ArgumentSpec;
import it.bitrule.plorex.commands.util.Predicates;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class FactionRegistry {

    @Getter private final static @NonNull FactionRegistry instance = new FactionRegistry();

    /**
     * The factions.
     * The key is the faction's identifier and the value is the faction.
     */
    private final @NonNull Map<UUID, Faction> factions = new ConcurrentHashMap<>();
    /**
     * The factions' name.
     * The key is the faction's name and the value is the faction's identifier.
     */
    private final @NonNull Map<String, UUID> factionNames = new HashMap<>();

    /**
     * The players' faction.
     * The key is the player's XUID and the value is the faction's identifier.
     */
    private final @NonNull Map<String, UUID> playersFaction = new ConcurrentHashMap<>();
    /**
     * The factions that need to be saved.
     */
    private final @NonNull Set<UUID> factionsDirty = ConcurrentHashMap.newKeySet();

    /**
     * Load all factions from the database.
     */
    public void loadAll() {
        for (FactionModel factionModel : Hunts.getFactionRepository().findAll()) {
            Faction faction = new Faction(UUID.fromString(factionModel.getIdentifier()), factionModel);
            this.registerNewFaction(faction);

            for (Map.Entry<String, FactionRole> entry : factionModel.getMembers().entrySet()) {
                ProfileModel profileModel = Hunts.getProfileRepository().findOne(entry.getKey()).orElse(null);
                if (profileModel == null || profileModel.getName() == null) continue;

                this.setPlayerFaction(
                        FactionMember.create(profileModel, entry.getValue()),
                        faction
                );
            }
        }
    }

    /**
     * Get the player's faction using the player's name.
     *
     * @param sourceName The name of the player.
     * @return The player's faction or null if the player is not in a faction.
     */
    public @Nullable Faction getFactionByPlayer(@NonNull String sourceName) {
        String sourceXuid = ProfileRegistry.getInstance().getPlayerXuid(sourceName);
        if (sourceXuid == null) return null;

        UUID factionId = this.playersFaction.get(sourceXuid);
        if (factionId == null) return null;

        return this.factions.get(factionId);
    }

    /**
     * Get the player's faction using their object.
     * Usually used when the player is online.
     *
     * @param source The player object.
     * @return The player's faction or null if the player is not in a faction.
     */
    public @Nullable Faction getFactionByPlayer(@NonNull Player source) {
        UUID factionId = this.playersFaction.get(source.getLoginChainData().getXUID());
        if (factionId == null) return null;

        return this.factions.get(factionId);
    }

    /**
     * Get a faction by its name.
     *
     * @param name The name of the faction.
     * @return The faction or null if the faction does not exist.
     */
    public @Nullable Faction getFactionByName(@NonNull String name) {
        UUID factionId = this.factionNames.get(name.toLowerCase());
        if (factionId == null) return null;

        return this.factions.get(factionId);
    }

    /**
     * Register a new faction.
     *
     * @param faction The faction to register.
     */
    public void registerNewFaction(@NonNull Faction faction) {
        this.factions.put(faction.getConvertedId(), faction);
        this.factionNames.put(faction.getModel().getName().toLowerCase(), faction.getConvertedId());
    }

    /**
     * Set the player's faction.
     *
     * @param factionMember The faction member.
     * @param faction The faction or null if the player is not in a faction.
     */
    public void setPlayerFaction(@NonNull FactionMember factionMember, @NonNull Faction faction) {
        ProfileRegistry.getInstance().setPlayerXuid(factionMember.getName(), factionMember.getXuid());

        this.playersFaction.put(factionMember.getXuid(), faction.getConvertedId());

        faction.addMember(factionMember);
    }

    /**
     * Clear the player's faction.
     *
     * @param factionMember The faction member.
     */
    public void clearPlayerFaction(@NonNull FactionMember factionMember) {
        ProfileRegistry.getInstance().removePlayerXuid(factionMember.getName());

        this.playersFaction.remove(factionMember.getXuid());
    }

    /**
     * Mark a faction as dirty.
     * This means that the faction needs to be saved.
     *
     * @param faction The faction to mark as dirty.
     */
    public void markFactionDirty(@NonNull Faction faction) {
        if (this.factionsDirty.contains(faction.getConvertedId())) return;

        this.factionsDirty.add(faction.getConvertedId());
    }

    /**
     * Create the main command.
     * Add all the faction commands to the main command.
     *
     * @return The main command.
     */
    public @NonNull MainCommand createMainCommand() {
        MainCommand mainCommand = new MainCommand(
                "team",
                "Team commands",
                new String[] {"t", "faction", "f"},
                ArgumentSpec.of(
                        "/<label> help",
                        Predicates.not(0)
                )
        );

        mainCommand.registerArgument(new FactionDisbandArgument());
        mainCommand.registerArgument(new FactionDepositArgument());
        mainCommand.registerArgument(new FactionPromoteArgument());
        mainCommand.registerArgument(new FactionSetHomeArgument());
        mainCommand.registerArgument(new FactionCreateArgument());
        mainCommand.registerArgument(new FactionInviteArgument());
        mainCommand.registerArgument(new FactionDemoteArgument());
        mainCommand.registerArgument(new FactionKickArgument());
        mainCommand.injectSuggestions();

        return mainCommand;
    }

    /**
     * Save all factions.
     *
     * @param wait Whether to wait for the factions to be saved.
     */
    public void saveAll(boolean wait) {
        AtomicInteger counter = new AtomicInteger(this.factionsDirty.size());

        for (UUID factionId : this.factionsDirty) {
            try {
                Faction faction = this.factions.get(factionId);
                if (faction == null) continue;

                Hunts.getFactionRepository().save(faction.getModel());
            } finally {
                counter.decrementAndGet();
            }
        }

        while (wait && counter.get() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        this.factionsDirty.clear();
    }
}