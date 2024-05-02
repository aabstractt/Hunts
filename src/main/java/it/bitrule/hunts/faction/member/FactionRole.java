package it.bitrule.hunts.faction.member;

import lombok.NonNull;

public enum FactionRole {
    MEMBER, OFFICER, LEADER;

    /**
     * Check if the role is above or equals than the given role.
     *
     * @param role The role to compare.
     * @return True if the role is above or equals than the given role.
     */
    public boolean isAboveOrEqualsThan(@NonNull FactionRole role) {
        return this.ordinal() >= role.ordinal();
    }
}