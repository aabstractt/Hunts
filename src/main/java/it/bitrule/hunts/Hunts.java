package it.bitrule.hunts;

import cn.nukkit.plugin.PluginBase;
import it.bitrule.hunts.config.FactionsConfig;
import it.bitrule.hunts.config.YamlConfig;
import it.bitrule.hunts.faction.FactionModel;
import it.bitrule.hunts.listener.player.PlayerAsyncPreLoginListener;
import it.bitrule.hunts.listener.player.PlayerJoinListener;
import it.bitrule.hunts.listener.player.PlayerQuitListener;
import it.bitrule.hunts.profile.ProfileModel;
import it.bitrule.hunts.registry.FactionRegistry;
import it.bitrule.miwiklark.common.Miwiklark;
import it.bitrule.miwiklark.common.repository.Repository;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class Hunts extends PluginBase {

    private static @Nullable Hunts instance;

    public static @NonNull Hunts getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Hunts instance is not initialized");
        }

        return instance;
    }

    /**
     * The faction repository.
     */
    private static @Nullable Repository<FactionModel> factionRepository;
    /**
     * The profile repository.
     */
    private static @Nullable Repository<ProfileModel> profileRepository;
    /**
     * The YAML configuration parsed to an object.
     */
    private static @Nullable YamlConfig yamlConfig = null;

    /**
     * Get the faction repository.
     *
     * @return The faction repository.
     */
    public static @NonNull Repository<FactionModel> getFactionRepository() {
        if (factionRepository == null) {
            throw new IllegalStateException("Faction repository is not initialized");
        }

        return factionRepository;
    }

    /**
     * Get the profile repository.
     *
     * @return The profile repository.
     */
    public static @NonNull Repository<ProfileModel> getProfileRepository() {
        if (profileRepository == null) {
            throw new IllegalStateException("Profile repository is not initialized");
        }

        return profileRepository;
    }

    /**
     * Get the YAML configuration.
     *
     * @return The YAML configuration.
     */
    public static @NonNull YamlConfig getYamlConfig() {
        if (yamlConfig == null) {
            throw new IllegalStateException("YAML configuration is not initialized");
        }

        return yamlConfig;
    }

    @Override
    public void onLoad() {
        instance = this;

        this.saveDefaultConfig();

        String dbName = this.getConfig().getString("mongodb.dbname");
        if (dbName == null) {
            throw new IllegalStateException("Database name is not defined in the configuration");
        }

        factionRepository = Miwiklark.addRepository(
                FactionModel.class,
                dbName,
                "factions"
        );
        profileRepository = Miwiklark.addRepository(
                ProfileModel.class,
                dbName,
                "profiles"
        );

        FactionRegistry.getInstance().loadAll();

        TranslationKey.adjustIntern();
    }

    @Override
    public void onEnable() {
        yamlConfig = new YamlConfig(
                FactionsConfig.wrap(this.getConfig().getSection("factions"))
        );

        this.getServer().getPluginManager().registerEvents(new PlayerAsyncPreLoginListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        this.getServer().getCommandMap().register("team", FactionRegistry.getInstance().createMainCommand());
    }

    @Override
    public void onDisable() {
        FactionRegistry.getInstance().saveAll(true);

        Promise.shutdown();
    }
}