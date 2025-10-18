package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.Rewards;
import me.soapiee.common.data.rewards.types.Reward;
import me.soapiee.common.logic.Checker;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import javax.naming.CommunicationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final BiomeMastery main;
    private final HikariCPConnection database;
    private final Logger logger;
    private final String dataSaveType;

    private final HashMap<UUID, PlayerData> playerDataMap;
    private final HashMap<Biome, BiomeData> biomeDataMap;
    private final List<String> enabledWorlds;
    private final List<Biome> enabledBiomes;
    private final HashMap<Integer, Integer> defaultLevels;
    private final HashMap<Integer, Reward> defaultRewards;
    private int updateInterval;
    private Checker progressChecker;

    public DataManager(BiomeMastery main) throws IOException, SQLException, CommunicationException {
        this.main = main;
        logger = main.getCustomLogger();
        playerDataMap = new HashMap<>();
        biomeDataMap = new HashMap<>();
        enabledWorlds = new ArrayList<>();
        enabledBiomes = new ArrayList<>();
        defaultLevels = new HashMap<>();
        defaultRewards = new HashMap<>();

        if (main.getConfig().getBoolean("database.enabled")) {
            dataSaveType = "database";
            database = new HikariCPConnection(main);
            database.connect();
        } else {
            database = null;
            dataSaveType = "files";

            try {
                Files.createDirectories(Paths.get(main.getDataFolder() + File.separator + "playerData"));
            } catch (IOException e) {
                throw new IOException(e);
            }

            Utils.consoleMsg(ChatColor.DARK_GREEN + "File Storage enabled.");
        }

        setDefaultSettings(Bukkit.getConsoleSender());
        createBiomeData(Bukkit.getConsoleSender());
        startChecker();
    }

    public void reload(CommandSender sender) {
        setDefaultSettings(sender);
        createBiomeData(sender);
        startChecker();
    }

    private void setDefaultSettings(CommandSender sender) {
        FileConfiguration config = main.getConfig();

        updateInterval = config.getInt("default_settings.update_interval");

        //Create list of enabled worlds
        enabledWorlds.clear();
        enabledWorlds.addAll(config.getStringList("default_settings.enabled_worlds"));

        //Create default levels + rewards
        defaultLevels.clear();
        defaultRewards.clear();
        for (String key : config.getConfigurationSection("default_settings.levels").getKeys(false)) {
            defaultLevels.put(Integer.parseInt(key), config.getInt("default_settings.levels." + key + "target_duration"));

            //TODO: See BiomeData todo
            defaultRewards.put(Integer.parseInt(key), new Rewards(main, sender, "default_settings.levels." + key + "."));
        }

        //Make list of enabled biomes + create the biome data
        enabledBiomes.clear();
        boolean whiteList = config.getBoolean("default_settings.use_blacklist_as_whitelist");
        List<String> listedBiomes = config.getStringList("default_settings.biomes_blacklist");

        if (whiteList) enabledBiomes.addAll(createBiomeWhitelist(sender, listedBiomes));
        else enabledBiomes.addAll(createBiomeBlacklist(listedBiomes));
    }

    public List<Biome> createBiomeBlacklist(List<String> listedBiomes) {
        List<Biome> blacklist = new ArrayList<>();

        for (Biome biome : Biome.values()) {
            if (listedBiomes.contains(biome.name())) continue;
//            if (listedBiomes.contains(biome.name().toLowerCase())) continue;
            blacklist.add(biome);
        }

        return blacklist;
    }

    public List<Biome> createBiomeWhitelist(CommandSender sender, List<String> listedBiomes) {
        List<Biome> whitelist = new ArrayList<>();

        for (String rawBiome : listedBiomes) {
            Biome biome;

            try {
                biome = Biome.valueOf(rawBiome);
            } catch (IllegalArgumentException e) {
                logger.logToPlayer(sender, e, "&c'" + rawBiome + "' is not a valid biome");
                continue;
            }

            whitelist.add(biome);
        }

        return whitelist;
    }

    private void createBiomeData(CommandSender sender) {
        //Loops through the list of enabled biomes. Find it in the biomes config section, if it doesnt exist then set default settings
        for (Biome enabledBiome : enabledBiomes) {
            boolean isDefault = main.getConfig().getConfigurationSection("biomes." + enabledBiome.name()) == null;

            BiomeData biomeData = new BiomeData(main, enabledBiome, isDefault, sender);
            biomeDataMap.put(enabledBiome, biomeData);
        }
    }

    public void startChecker() {
        if (progressChecker != null)
            if (!progressChecker.isCancelled()) progressChecker.cancel();

        progressChecker = new Checker(main, updateInterval);
    }

    public void add(PlayerData data) {
        playerDataMap.put(data.uuid, data);
    }

    public void remove(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public boolean has(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public String getDataSaveType() {
        return dataSaveType;
    }

    public HikariCPConnection getDatabase() {
        return database;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public List<String> getEnabledWorlds() {
        return enabledWorlds;
    }

    public BiomeData getBiomeData(Biome biome) {
        return biomeDataMap.get(biome);
    }

    public HashMap<Integer, Integer> getDefaultLevels() {
        return defaultLevels;
    }

    public HashMap<Integer, Reward> getDefaultRewards() {
        return defaultRewards;
    }

    public void saveAll(boolean async) {
        for (UUID uuid : playerDataMap.keySet()) {
            getPlayerData(uuid).saveData(async);
        }
    }

}
