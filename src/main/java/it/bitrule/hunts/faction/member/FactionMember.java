package it.bitrule.hunts.faction.member;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class FactionMember {

    private final @NonNull String xuid;
    private @NonNull FactionRole role;

    private @NonNull String name;
    private int kills;
    private int deaths;
}