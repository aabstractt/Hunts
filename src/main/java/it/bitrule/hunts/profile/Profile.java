package it.bitrule.hunts.profile;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Data
public final class Profile {

    /**
     * The profile model.
     */
    private final @NonNull ProfileModel model;

    /**
     * Whether the profile is dirty.
     */
    private boolean dirty = false;

    /**
     * Mark the profile as dirty.
     * This means that the profile has been modified and needs to be saved.
     */
    public void setDirty() {
        this.dirty = true;
    }

    public void afterSave() {
        this.dirty = false;
    }
}