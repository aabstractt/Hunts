package it.bitrule.hunts.faction;

import it.bitrule.hunts.faction.member.FactionMember;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class Faction {

    private final @NonNull FactionModel model; // TODO: I need make some things to prevent accessing to the model from outside.

    /**
     * Add a member to the faction.
     *
     * @param factionMember The member to add.
     */
    public void addMember(@NonNull FactionMember factionMember) {
        this.model.getMembers().put(factionMember.getXuid(), factionMember.getRole());
    }

    /**
     * Create a new faction from the faction model.
     *
     * @param factionModel The faction model.
     * @return The faction.
     */
    public static @NonNull Faction from(@NonNull FactionModel factionModel) {
        return new Faction(factionModel);
    }
}