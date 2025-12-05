package me.soapiee.common.manager;

import lombok.Getter;
import lombok.Setter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.effects.Effect;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardFactory;
import me.soapiee.common.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ConfigManager {

    private FileConfiguration config;

    @Getter @Setter private boolean databaseEnabled;
    @Getter private boolean debugMode;
    @Getter private boolean updateNotif;
    @Getter private final HashSet<World> enabledWorlds = new HashSet<>();
    @Getter private final HashSet<Biome> enabledBiomes = new HashSet<>();
    @Getter private final HashMap<Integer, Integer> defaultLevelsThresholds = new HashMap<>();
    @Getter private final HashMap<Integer, Reward> defaultRewards = new HashMap<>();
    @Getter private final HashMap<String, Effect> effects = new HashMap<>();
    @Getter private int updateInterval;

    public ConfigManager(FileConfiguration config, Logger logger) {
        this.config = config;
        databaseEnabled = config.getBoolean("database.enabled", false);
        debugMode = config.getBoolean("debug_mode", false);
        updateNotif = config.getBoolean("settings.plugin_update_notification", true);
        updateInterval = config.getInt("settings.update_interval", 60);

        enabledWorlds.addAll(setUpEnabledWords());
        enabledBiomes.addAll(setUpEnabledBiomes());
    }

    public ArrayList<World> setUpEnabledWords() {
        //Create list of enabled worlds
        ArrayList<World> worldList = new ArrayList<>();
        boolean worldsListExists = config.isSet("default_biome_settings.enabled_worlds");
        if (worldsListExists) {
            for (String worldString : config.getStringList("default_biome_settings.enabled_worlds")) {
                World world = Bukkit.getWorld(worldString);
                if (world != null) worldList.add(world);
            }
        }
        return worldList;
    }

    public void setUpDefaultRewards(RewardFactory rewardFactory) {
        ConfigurationSection levelsSection = config.getConfigurationSection("default_biome_settings.levels");
        if (levelsSection != null) {
            for (String key : config.getConfigurationSection("default_biome_settings.levels").getKeys(false)) {
                defaultLevelsThresholds.put(Integer.parseInt(key), config.getInt("default_biome_settings.levels." + key + ".target_duration"));
                defaultRewards.put(Integer.parseInt(key), rewardFactory.create("default_biome_settings.levels." + key + "."));
            }
        }
    }

    public List<Biome> setUpEnabledBiomes() {
        //Make list of enabled biomes + create the biome data
        boolean whiteList = config.getBoolean("default_biome_settings.use_blacklist_as_whitelist", true);
        if (!config.isSet("default_biome_settings.biomes_blacklist")) {
            config.set("default_biome_settings.biomes_blacklist", new ArrayList<>());
        }
        List<String> listedBiomes = config.getStringList("default_biome_settings.biomes_blacklist");

        if (whiteList) return createBiomeWhitelist(listedBiomes);
        else return createBiomeBlacklist(listedBiomes);
    }

    public void reload(BiomeMastery main, DataManager dataManager) {
        main.reloadConfig();
        config = main.getConfig();
        updateInterval = config.getInt("settings.update_interval", 60);
        debugMode = config.getBoolean("debug_mode", false);
        dataManager.getCooldownManager().updateThreshold(config.getInt("settings.command_cooldown", 3));
    }

    public List<Biome> createBiomeBlacklist(List<String> listedBiomes) {
        List<Biome> blacklist = new ArrayList<>();

        for (Biome biome : Biome.values()) {
            if (listedBiomes.contains(biome.name())) continue;
            blacklist.add(biome);
        }

        return blacklist;
    }

    public List<Biome> createBiomeWhitelist(List<String> listedBiomes) {
        List<Biome> whitelist = new ArrayList<>();

        for (String rawBiome : listedBiomes) {
            Biome biome = validateBiome(rawBiome);
            if (biome != null) whitelist.add(biome);
        }

        return whitelist;
    }

    private Biome validateBiome(String string) {
        Biome biome;

        try {
            biome = Biome.valueOf(string.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        return biome;
    }

    public boolean isEnabledWorld(World world) {
        return enabledWorlds.contains(world);
    }

    public boolean isEnabledBiome(Biome biome) {
        return enabledBiomes.contains(biome);
    }
}
