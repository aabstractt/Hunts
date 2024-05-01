package it.bitrule.hunts.faction;

import it.bitrule.hunts.faction.member.FactionMember;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor @Data
public final class Faction {

    /**
     * The identifier of the faction converted to UUID.
     */
    private final @NonNull UUID convertedId;
    /**
     * The storage model of the faction.
     */
    private final @NonNull FactionModel model; // TODO: I need make some things to prevent accessing to the model from outside.

    /**
     * The members of the faction converted to an object.
     */
    private final @NonNull Set<FactionMember> factionMembers = new HashSet<>();

    /**
     * Add a member to the faction.
     *
     * @param factionMember The member to add.
     */
    public void addMember(@NonNull FactionMember factionMember) {
        this.model.getMembers().put(factionMember.getXuid(), factionMember.getRole());

        this.factionMembers.add(factionMember);
    }

    /**
     * Remove a member from the faction.
     *
     * @param xuid The XUID of the member to remove.
     */
    public void removeMember(@NonNull String xuid) {
        this.model.getMembers().remove(xuid);

        this.factionMembers.removeIf(factionMember -> factionMember.getXuid().equals(xuid));
    }

    /**
     * Get a member by their XUID.
     *
     * @param xuid The XUID of the member.
     * @return The member or null if the member is not found.
     */
    public @Nullable FactionMember getMemberByXuid(@NonNull String xuid) {
        return this.factionMembers.stream()
                .filter(member -> member.getXuid().equals(xuid)).findFirst()
                .orElse(null);
    }

    /**
     * Get a member by their name.
     *
     * @param name The name of the member.
     * @return The member or null if the member is not found.
     */
    public @Nullable FactionMember getMemberByName(@NonNull String name) {
        return this.factionMembers.stream()
                .filter(factionMember -> factionMember.getName().equalsIgnoreCase(name)).findFirst()
                .orElse(null);
    }

    /**
     * Create a new faction from the faction model.
     *
     * @param factionModel The faction model.
     * @return The faction.
     */
    public static @NonNull Faction from(@NonNull FactionModel factionModel) {
        return new Faction(UUID.fromString(factionModel.getIdentifier()), factionModel);
    }
}