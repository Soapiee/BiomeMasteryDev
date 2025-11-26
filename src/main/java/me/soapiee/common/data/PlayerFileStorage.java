package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.manager.BiomeDataManager;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

public class PlayerFileStorage implements PlayerStorageHandler {

    private final BiomeMastery main;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final PlayerData playerData;
    private final Logger customLogger;
    private final Object fileLock = new Object();

    private final File file;
    private YamlConfiguration contents;
    private final OfflinePlayer player;

    public PlayerFileStorage(BiomeMastery main, PlayerData playerData) {
        this.main = main;
        configManager = main.getDataManager().getConfigManager();
        biomeDataManager = main.getDataManager().getBiomeDataManager();
        this.playerData = playerData;
        customLogger = main.getCustomLogger();
        player = playerData.getPlayer();

        UUID uuid = playerData.getPlayer().getUniqueId();
        file = new File(main.getDataFolder() + File.separator + "Data" + File.separator + "BiomeLevels", uuid + ".yml");

        createPlayerLevels();
    }

    private void createPlayerLevels() {
        for (Biome key : configManager.getEnabledBiomes()) {
            playerData.getBiomesMap().put(key, new BiomeLevel(player, biomeDataManager.getBiomeData(key)));
        }
    }

    @Override
    public void readData() {
        if (!file.exists()) {
            createFile();
            return;
        }

        contents = YamlConfiguration.loadConfiguration(file);

        synchronized (fileLock) {
            boolean updated = false;

            for (Biome biome : configManager.getEnabledBiomes()) {
                String biomeName = biome.name();

                if (!contents.isSet(biomeName + ".Level") || !contents.isSet(biomeName + ".Progress")) {
                    contents.set(biomeName + ".Level", 0);
                    contents.set(biomeName + ".Progress", 0);
                    updated = true;
                } else {
                    setBiomeLevelData(biome);
                }
            }

            if (updated) {
                try {
                    contents.save(file);
                } catch (IOException e) {
                    customLogger.logToFile(e, "Failed to update missing biome data for " + player.getName());
                }
            }
        }
    }

    @Override
    public void saveData(boolean async) {
        Runnable task = () -> saveRunnable(player.getName());
        if (async) new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        }.runTaskAsynchronously(main);
        else saveRunnable(player.getName());
    }

    private void setBiomeLevelData(Biome biome) {
        String biomeName = biome.name();

        int level = contents.getInt(biomeName + ".Level", 0);
        int progress = contents.getInt(biomeName + ".Progress", 0);
        playerData.getBiomesMap().get(biome).setLevel(level);
        playerData.getBiomesMap().get(biome).setProgress(progress);

        if (configManager.isDebugMode()) Utils.debugMsg(player.getName(),
                ChatColor.GREEN + biomeName + " data set (" + level + ":" + progress + ")");
    }

    private void createFile() {
        final String playerName = player.getName();
        final HashSet<String> biomes = new HashSet<>();
        for (Biome biome : configManager.getEnabledBiomes()) {
            biomes.add(biome.name());
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (fileLock) {

                    try {
                        YamlConfiguration localCopy = new YamlConfiguration();
                        contents = localCopy;

                        for (String biome : biomes) {
                            localCopy.set(biome + ".Level", 0);
                            localCopy.set(biome + ".Progress", 0);
                        }

                        localCopy.save(file);
                    } catch (IOException e) {
                        customLogger.logToFile(e, "Could not create new data for " + playerName);
                    }
                }
            }
        }.runTaskAsynchronously(main);

        for (Biome biome : configManager.getEnabledBiomes()) {
            playerData.getBiomesMap().put(biome, new BiomeLevel(player, biomeDataManager.getBiomeData(biome)));
            if (configManager.isDebugMode()) Utils.debugMsg(player.getName(),
                    ChatColor.GREEN + biome.name() + " data set (0:0)");
        }
    }

    private void saveRunnable(final String playerName) {
        YamlConfiguration localCopy = contents;

        synchronized (fileLock) {
            for (Biome biomeKey : playerData.getBiomesMap().keySet()) {
                String biome = biomeKey.name();
                BiomeLevel level = playerData.getBiomeLevel(biomeKey);
                localCopy.set(biome + ".Level", level.getLevel());
                localCopy.set(biome + ".Progress", level.getProgress());
            }
        }

        try {
            localCopy.save(file);
        } catch (IOException e) {
            customLogger.logToFile(e, main.getMessageManager().getWithPlaceholder(Message.DATAERROR, playerName));
        }
    }

    //    =-=-=-=-=-=-=-=-=-=-=-=-= BIOME DATA POST GROUP UPDATE =-=-=-=-=-=-=-=-=-=-=-=-=
//    @Override
//    public void readData() {
//        if (!file.exists()) {
//            createFile();
//            return;
//        }
//
//        contents = YamlConfiguration.loadConfiguration(file);
//
//        synchronized (fileLock) {
//            boolean updated = false;
//
//            for (BiomeData biomeData : biomeDataManager.getBiomeDataMap().values()) {
//                String biomeName = biomeData.getBiomeName();
//                Biome biome = biomeData.getBiome();
//
//                if (!contents.isSet(biomeName + ".Level") || !contents.isSet(biomeName + ".Progress")) {
//                    contents.set(biomeName + ".Level", 0);
//                    contents.set(biomeName + ".Progress", 0);
//                    updated = true;
//
//                } else {
//                    setBiomeLevelData(biome);
//                }
//            }
//
//            if (updated) {
//                try {
//                    contents.save(file);
//                } catch (IOException e) {
//                    customLogger.logToFile(e, "Failed to update missing biome data for " + player.getName());
//                }
//            }
//        }
//    }

//    private void createPlayerLevels(){
//        if (configManager.isBiomesGrouped()){
//            for (Biome parentBiome : configManager.getGroupBiomes().keySet()) {
//                playerData.getBiomesMap().put(parentBiome, new BiomeLevel(player, biomeDataManager.getBiomeData(parentBiome)));
//            }
//        }
//
//        for (BiomeData biomeData : biomeDataManager.getBiomeDataMap().values()) {
//            BiomeLevel level;
//
//            if (biomeData.isChild()){
//                Biome parentBiome = biomeData.getParent();
//                level = playerData.getBiomesMap().get(parentBiome);
//            } else {
//                level = new BiomeLevel(player, biomeData);
//            }
//
//            playerData.getBiomesMap().put(biomeData.getBiome(), level);
//        }
//    }
}
