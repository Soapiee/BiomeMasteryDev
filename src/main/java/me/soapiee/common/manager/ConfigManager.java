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

    private final RewardFactory rewardFactory;
    private FileConfiguration config;
    private final Logger logger;

    @Getter @Setter private boolean databaseEnabled;
    @Getter private boolean debugMode;
    @Getter private boolean updateNotif;
    @Getter private final HashSet<World> enabledWorlds = new HashSet<>();
    @Getter private final HashSet<Biome> enabledBiomes = new HashSet<>();
    @Getter private final HashMap<Integer, Integer> defaultLevelsThresholds = new HashMap<>();
    @Getter private final HashMap<Integer, Reward> defaultRewards = new HashMap<>();
    @Getter private final HashMap<String, Effect> effects = new HashMap<>();
    @Getter private int updateInterval;

//    @Getter private final boolean biomesGrouped;
//    @Getter private final HashMap<Biome, ArrayList<Biome>> groupBiomes = new HashMap<>();

    public ConfigManager(FileConfiguration config, RewardFactory rewardFactory, Logger logger) {
        this.config = config;
        this.rewardFactory = rewardFactory;
        this.logger = logger;
        databaseEnabled = config.getBoolean("database.enabled", false);
        debugMode = config.getBoolean("debug_mode", false);
        updateNotif = config.getBoolean("settings.plugin_update_notification", true);
        updateInterval = config.getInt("settings.update_interval", 60);

        setDefaultSettings();
//        biomesGrouped = validateBiomeGroups();
//        createGroupBiomes();
    }

    private void setDefaultSettings() {
        //Create list of enabled worlds
        enabledWorlds.clear();
        ArrayList<World> worldList = new ArrayList<>();
        boolean worldsListExists = config.isSet("default_biome_settings.enabled_worlds");
        if (worldsListExists) {
            for (String worldString : config.getStringList("default_biome_settings.enabled_worlds")) {
                World world = Bukkit.getWorld(worldString);
                if (world != null) worldList.add(world);
            }
        }
        enabledWorlds.addAll(worldList);

        //Create default levels + rewards
        defaultLevelsThresholds.clear();
        defaultRewards.clear();

        ConfigurationSection levelsSection = config.getConfigurationSection("default_biome_settings.levels");
        if (levelsSection != null) {
            for (String key : config.getConfigurationSection("default_biome_settings.levels").getKeys(false)) {
                defaultLevelsThresholds.put(Integer.parseInt(key), config.getInt("default_biome_settings.levels." + key + ".target_duration"));
                defaultRewards.put(Integer.parseInt(key), rewardFactory.create("default_biome_settings.levels." + key + "."));
            }
        }

        //Make list of enabled biomes + create the biome data
        enabledBiomes.clear();
        boolean whiteList = config.getBoolean("default_biome_settings.use_blacklist_as_whitelist", true);
        if (!config.isSet("default_biome_settings.biomes_blacklist")) {
            config.set("default_biome_settings.biomes_blacklist", new ArrayList<>());
        }
        List<String> listedBiomes = config.getStringList("default_biome_settings.biomes_blacklist");

        if (whiteList) enabledBiomes.addAll(createBiomeWhitelist(listedBiomes));
        else enabledBiomes.addAll(createBiomeBlacklist(listedBiomes));
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
        } catch (IllegalArgumentException e) {
            logger.logToFile(e, "&c'" + string + "' is not a valid biome");
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

    //    private boolean validateBiomeGroups() {
//        if (!config.isConfigurationSection("groups") || config.getConfigurationSection("groups").getKeys(false).isEmpty()) {
//            if (debugMode) Utils.debugMsg("", ChatColor.RED + "No biome groups set");
//            return false;
//        }
//
//        //Check there are no duplicate biomes in the biome groups config section
//        HashSet<Biome> groupBiomes = new HashSet<>();
//        for (String groupName : config.getConfigurationSection("groups").getKeys(false)){
//            for (String biomeName : config.getStringList("groups." + groupName)){
//                Biome biome = validateBiome(biomeName);
//                if (biome == null) continue;
//
//                if (groupBiomes.contains(biome)){
//                    logger.logToFile(null, "&c'" + biomeName + "' biome appears more than once, in the group lists. \nA biome cannot be in more than one group");
//                    return false;
//                }
//
//                groupBiomes.add(biome);
//            }
//        }
//
//        //Check all the parent and child biomes are enabled biomes
//        for (Biome groupBiome : groupBiomes){
//            if (!enabledBiomes.contains(groupBiome)) {
//                logger.logToFile(null, "&c'" + groupBiome.name() + "' is not an enabled biome, so it cannot be in a group");
//                return false;
//            }
//        }
//
//        return true;
//    }

//    private void createGroupBiomes() {
//        for (String groupName : config.getConfigurationSection("groups").getKeys(false)){
//            Biome parentBiome = validateBiome(groupName);
//            if (parentBiome == null) continue;
//
//            ArrayList<Biome> childList = new ArrayList<>();
//            for (String childName : config.getStringList("groups." + groupName)){
//                Biome childBiome = validateBiome(childName);
//                if (childBiome == null) continue;
//
//                childList.add(childBiome);
//            }
//            if (childList.isEmpty()) continue;
//
//            groupBiomes.put(parentBiome, childList);
//        }
//    }
}
