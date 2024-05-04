package it.bitrule.hunts.profile;

import lombok.*;

@RequiredArgsConstructor @Data
public final class Profile {

    /**
     * The profile model.
     */
    private final @NonNull ProfileModel model;

    /**
     * Whether the profile is dirty.
     */
    @Setter (value = AccessLevel.PRIVATE) private boolean dirty = false;

    /**
     * Mark the profile as dirty.
     * This means that the profile has been modified and needs to be saved.
     */
    public void setDirty() {
        this.dirty = true;
    }

    /**
     * Notify that the profile has been saved.
     */
    public void notifySaved() {
        this.dirty = false;
    }
}