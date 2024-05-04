package it.bitrule.hunts.profile;

import com.google.gson.annotations.SerializedName;
import it.bitrule.miwiklark.common.repository.model.IModel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor @Data
public final class ProfileModel implements IModel {

    /**
     * The identifier of the profile.
     */
    @SerializedName("_id")
    private final String identifier;

    /**
     * The current name of the profile.
     */
    private @Nullable String name;
    /**
     * The last name of the profile.
     */
    private @Nullable String lastName;
    /**
     * The balance of the profile.
     */
    private int balance;
    /**
     * The kills of the profile.
     */
    private int kills;
    /**
     * The deaths of the profile.
     */
    private int deaths;
}