package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.Checker;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
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
    private int updateInterval;
    private Checker progressChecker;

    public DataManager(BiomeMastery main) throws IOException, SQLException, CommunicationException {
        this.main = main;
        logger = main.getCustomLogger();
        playerDataMap = new HashMap<>();
        biomeDataMap = new HashMap<>();
        enabledWorlds = new ArrayList<>();
        enabledBiomes = new ArrayList<>();

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

        setDefaultSettings();
        createBiomeData();
        startChecker();
    }

    public void reload() {
        setDefaultSettings();
        createBiomeData();
        startChecker();
    }

    private void createBiomeData() {
        //Loops through the list of enabled biomes. Find it in the biomes config section, if it doesnt exist then set default settings
        for (Biome enabledBiome : enabledBiomes) {
            boolean isDefault = main.getConfig().getConfigurationSection("biomes." + enabledBiome.name()) != null;

            BiomeData biomeData = new BiomeData(main, enabledBiome, isDefault);
            biomeDataMap.put(enabledBiome, biomeData);
        }
    }

    private void setDefaultSettings() {
        FileConfiguration config = main.getConfig();

        updateInterval = config.getInt("default_settings.update_interval");
        enabledWorlds.clear();
        enabledWorlds.addAll(config.getStringList("default_settings.enabled_worlds"));
        enabledBiomes.clear();

        //Make list of enabled biomes
        boolean whiteList = config.getBoolean("default_settings.use_blacklist_as_whitelist");
        List<String> listedBiomes = config.getStringList("default_settings.biomes_blacklist");

        if (whiteList) {
            for (String rawBiome : listedBiomes) {
                Biome biome;
                try {
                    biome = Biome.valueOf(rawBiome);
                } catch (IllegalArgumentException e) {
                    //TODO: Set up logger and throw error to console + player
//                throw new IllegalArgumentException();
                    continue;
                }
                enabledBiomes.add(biome);
            }
        } else {
            for (Biome biome : Biome.values()) {
                if (listedBiomes.contains(biome.name())) continue;
                enabledBiomes.add(biome);
            }
        }

        setDefaultLevels();
        setDefaultRewards();
    }

    public void setDefaultLevels() {
        for (String key : main.getConfig().getConfigurationSection("default_settings.levels").getKeys(false)) {
            defaultLevels
        }

    }

    public void setDefaultRewards() {

    }

    public HashMap<Integer, Integer> getDefaultLevels() {
        return defaultLevels();
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

    public void saveAll(boolean async) {
        for (UUID uuid : playerDataMap.keySet()) {
            getPlayerData(uuid).saveData(async);
        }
    }

}
