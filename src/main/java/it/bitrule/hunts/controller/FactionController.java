package it.bitrule.hunts.controller;

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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class FactionController {

    @Getter private final static @NonNull FactionController instance = new FactionController();

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
            this.cache(faction);

            for (Map.Entry<String, FactionRole> entry : factionModel.getMembers().entrySet()) {
                ProfileModel profileModel = Hunts.getProfileRepository().findOne(entry.getKey()).orElse(null);
                if (profileModel == null || profileModel.getName() == null) continue;

                faction.addMember(FactionMember.create(profileModel, entry.getValue()));
                this.cacheMember(profileModel.getIdentifier(), faction.getConvertedId());

                // Cache the player's XUID using the player's name
                ProfileController.getInstance().cacheXuid(profileModel.getName(), profileModel.getIdentifier());
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
        String sourceXuid = ProfileController.getInstance().getPlayerXuid(sourceName);
        if (sourceXuid == null) return null;

        return this.getFactionByPlayerXuid(sourceXuid);
    }

    /**
     * Get the player's faction using their object.
     * Usually used when the player is online.
     *
     * @param source The player object.
     * @return The player's faction or null if the player is not in a faction.
     */
    public @Nullable Faction getFactionByPlayer(@NonNull Player source) {
        return this.getFactionByPlayerXuid(source.getLoginChainData().getXUID());
    }

    /**
     * Get the player's faction using the player's XUID.
     *
     * @param sourceXuid The XUID of the player.
     * @return The player's faction or null if the player is not in a faction.
     */
    public @Nullable Faction getFactionByPlayerXuid(@NonNull String sourceXuid) {
        UUID factionId = this.playersFaction.get(sourceXuid);
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
     * Get all factions.
     *
     * @return All factions.
     */
    public @NonNull Collection<Faction> getAll() {
        return this.factions.values();
    }

    /**
     * Register a new faction.
     *
     * @param faction The faction to register.
     */
    public void cache(@NonNull Faction faction) {
        this.factions.put(faction.getConvertedId(), faction);
        this.factionNames.put(faction.getModel().getName().toLowerCase(), faction.getConvertedId());
    }

    /**
     * Set the player's faction.
     *
     * @param sourceXuid The XUID of the player.
     * @param factionConvertedId The identifier of the faction.
     */
    public void cacheMember(@NonNull String sourceXuid, @NonNull UUID factionConvertedId) {
        this.playersFaction.put(sourceXuid, factionConvertedId);
    }

    /**
     * Clear the player's faction.
     *
     * @param sourceXuid The XUID of the player.
     */
    public void clearMember(@NonNull String sourceXuid) {
        this.playersFaction.remove(sourceXuid);
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

        mainCommand.registerArgument(new FactionTransferArgument());
        mainCommand.registerArgument(new FactionDisbandArgument());
        mainCommand.registerArgument(new FactionDepositArgument());
        mainCommand.registerArgument(new FactionPromoteArgument());
        mainCommand.registerArgument(new FactionSetHomeArgument());
        mainCommand.registerArgument(new FactionCreateArgument());
        mainCommand.registerArgument(new FactionInviteArgument());
        mainCommand.registerArgument(new FactionDemoteArgument());
        mainCommand.registerArgument(new FactionLeaveArgument());
        mainCommand.registerArgument(new FactionKickArgument());
        mainCommand.registerArgument(new FactionJoinArgument());
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