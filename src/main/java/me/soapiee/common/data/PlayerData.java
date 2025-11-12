package me.soapiee.common.data;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.rewards.types.EffectReward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.logic.rewards.types.Reward;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final MessageManager messageManager;
    private final Logger logger;
    private final Object fileLock = new Object();

    private final UUID uuid;
    @Getter private final OfflinePlayer player;
    private final Map<Biome, BiomeLevel> biomesMap;
    @Getter private final ArrayList<Reward> activeRewards;

    private File file;
    private YamlConfiguration contents;

    public PlayerData(BiomeMastery main, @NotNull OfflinePlayer player) throws IOException, SQLException {
        this.main = main;
        dataManager = main.getDataManager();
        messageManager = main.getMessageManager();
        logger = main.getCustomLogger();
        uuid = player.getUniqueId();
        this.player = player;
        biomesMap = new ConcurrentHashMap<>();
        activeRewards = new ArrayList<>();

        switch (dataManager.getDataSaveType()) {
            case "database":
                readData();
                break;

            case "files":
                file = new File(main.getDataFolder() + File.separator + "Data" + File.separator + "BiomeLevels", uuid + ".yml");
                contents = new YamlConfiguration();

                readFile();
                break;
        }
    }

//  +_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+ FILE STORAGE METHODS +_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    private void readFile() {
        synchronized (fileLock) {
            if (!file.exists()) {
                createFile();
                return;
            }
            contents = YamlConfiguration.loadConfiguration(file);
        }

        for (Biome biome : dataManager.getEnabledBiomes()) {
            final String playerName = player.getName();
            final String biomeName = biome.name();

            if (!contents.isConfigurationSection(biomeName) || !contents.isSet(biomeName + ".Level") || !contents.isSet(biomeName + ".Progress")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        synchronized (fileLock) {
                            try {
                                FileConfiguration localCopy = YamlConfiguration.loadConfiguration(file);

                                localCopy.set(biomeName + ".Level", 0);
                                localCopy.set(biomeName + ".Progress", 0);

                                localCopy.save(file);
                            } catch (IOException e) {
                                main.getCustomLogger().logToFile(e, "Could not set " + biomeName + " config section for " + playerName);
                            }
                        }

                        BiomeLevel biomeLevel = new BiomeLevel(player, dataManager.getBiomeData(biome));

                        biomesMap.put(biome, biomeLevel);
                        if (main.isDebugMode()) Utils.debugMsg(playerName,
                                ChatColor.GREEN + biomeName + " data set (0:0)");
                    }
                }.runTaskAsynchronously(main);

            } else {
                biomesMap.put(biome, readBiomeLevelData(biome));
            }
        }
    }

    private BiomeLevel readBiomeLevelData(Biome biome) {
        String biomeName = biome.name();

        int level = contents.getInt(biomeName + ".Level", 0);
        int progress = contents.getInt(biomeName + ".Progress", 0);

        if (main.isDebugMode()) Utils.debugMsg(player.getName(),
                ChatColor.GREEN + biomeName + " data set (" + level + ":" + progress + ")");

        return new BiomeLevel(player, dataManager.getBiomeData(biome), level, progress);
    }

    private void createFile() {
        final String playerName = player.getName();
        final HashSet<String> biomes = new HashSet<>();
        for (Biome biome : dataManager.getEnabledBiomes()) {
            biomes.add(biome.name());
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                synchronized (fileLock) {
                    try {
                        FileConfiguration localCopy = YamlConfiguration.loadConfiguration(file);

                        for (String biome : biomes) {
                            localCopy.set(biome + ".Level", 0);
                            localCopy.set(biome + ".Progress", 0);
                        }

                        localCopy.save(file);
                    } catch (IOException e) {
                        main.getCustomLogger().logToFile(e, "Could not create new data for " + playerName);
                    }
                }
            }
        }.runTaskAsynchronously(main);

        for (Biome biome : dataManager.getEnabledBiomes()) {
            biomesMap.put(biome, new BiomeLevel(player, dataManager.getBiomeData(biome)));
            if (main.isDebugMode()) Utils.debugMsg(player.getName(),
                    ChatColor.GREEN + biome.name() + " data set (0:0)");
        }
    }

    private void saveFile(boolean async) {
        for (final Biome biomeKey : biomesMap.keySet()) {
            final String biome = biomeKey.name();
            final int level = getBiomeLevel(biomeKey).getLevel();
            final long progress = getBiomeLevel(biomeKey).getProgress();
            final String playerName = player.getName();

            if (async) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        synchronized (fileLock) {
                            try {
                                FileConfiguration localCopy = YamlConfiguration.loadConfiguration(file);
                                localCopy.set(biome + ".Level", level);
                                localCopy.set(biome + ".Progress", progress);
                                localCopy.save(file);
                            } catch (IOException e) {
                                logger.logToFile(e, main.getMessageManager().getWithPlaceholder(Message.DATAERROR, playerName));
                            }
                        }
                    }
                }.runTaskAsynchronously(main);
            } else {
                synchronized (fileLock) {

                    try {
                        FileConfiguration localCopy = YamlConfiguration.loadConfiguration(file);
                        localCopy.set(biome + ".Level", level);
                        localCopy.set(biome + ".Progress", progress);
                        localCopy.save(file);
                    } catch (IOException e) {
                        logger.logToFile(e, main.getMessageManager().getWithPlaceholder(Message.DATAERROR, playerName));
                    }
                }
            }
        }
    }

//  +_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+ DATABASE METHODS +_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+_+

    private void readData(){
        for (Biome key : dataManager.getEnabledBiomes()) {
            biomesMap.put(key, new BiomeLevel(player, dataManager.getBiomeData(key)));
        }

        for (Biome key : dataManager.getEnabledBiomes()) {
            getPlayerData(key.name(), (player2, results, error) -> {
                if (error != null) {
                    logger.logToPlayer((CommandSender) player2, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                    return;
                }
                
                biomesMap.get(key).setLevel(results.getLevel());
                biomesMap.get(key).setProgress(results.getProgress());
            });
        }
    }

    private void getPlayerData(final String table, final Callback<BiomeLevel> callback) {
        final BiomeData biomeData = dataManager.getBiomeData(table);

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

            //Check it exists, and if not, create entry
            try (Connection connection = dataManager.getDatabase().getConnection().getConnection();
                 PreparedStatement existsStatement = connection.prepareStatement("SELECT * FROM " + table + " WHERE UUID=?")) {
                existsStatement.setString(1, uuid.toString());
                ResultSet results = existsStatement.executeQuery();

                BiomeLevel biomeLevel;

                //creates entry
                if (!results.next()) {
                    try (PreparedStatement createStatement = connection.prepareStatement("INSERT INTO " + table + " VALUES (?,?,?);")) {
                        createStatement.setString(1, uuid.toString());
                        createStatement.setInt(2, 0);
                        createStatement.setInt(3, 0);
                        createStatement.executeUpdate();
                    }
                    biomeLevel = new BiomeLevel(player, biomeData);
                    Bukkit.getScheduler().runTask(main, () -> callback.onQueryDone(player, biomeLevel, null));
                    return;
                }

                //Get data
                int level = results.getInt("LEVEL");
                int progress = results.getInt("PROGRESS");
                biomeLevel = new BiomeLevel(player, biomeData, level, progress);

                Bukkit.getScheduler().runTask(main, () -> callback.onQueryDone(player, biomeLevel, null));
            } catch (SQLException e) {
                Bukkit.getScheduler().runTask(main, () -> callback.onQueryDone(player, null, e));
            }
        });
    }

    private void saveDatabaseEntry(boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                for (final Biome biome : biomesMap.keySet()) {
                    try (Connection connection = dataManager.getDatabase().getConnection().getConnection();
                         PreparedStatement statement = connection.prepareStatement("UPDATE " + biome.name() + " SET LEVEL=?, PROGRESS=? WHERE UUID=?;")) {
                        statement.setInt(1, getBiomeLevel(biome).getLevel());
                        statement.setLong(2, getBiomeLevel(biome).getProgress());
                        statement.setString(3, uuid.toString());
                        statement.executeUpdate();

                    } catch (SQLException e) {
                        logger.logToFile(e, ChatColor.RED + player.getName() + "'s " + biome.name() + " data could not be saved");
                    }
                }
            });

        } else {
            for (final Biome biome : dataManager.getEnabledBiomes()) {
                try (Connection connection = dataManager.getDatabase().getConnection().getConnection();
                     PreparedStatement statement = connection.prepareStatement("UPDATE " + biome.name() + " SET LEVEL=?, PROGRESS=? WHERE UUID=?;")) {
                    statement.setInt(1, getBiomeLevel(biome).getLevel());
                    statement.setLong(2, getBiomeLevel(biome).getProgress());
                    statement.setString(3, uuid.toString());
                    statement.executeUpdate();

                } catch (SQLException e) {
                    logger.logToFile(e, ChatColor.RED + player.getName() + "'s " + biome.name() + " data could not be saved");
                }
            }
        }
    }

    //  -_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_  OBJECT METHODS _-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-

    public void saveData(boolean async) {
        if (dataManager.getDataSaveType().equalsIgnoreCase("database")) saveDatabaseEntry(async);
        else saveFile(async);
    }

    public BiomeLevel getBiomeLevel(Biome biome) {
        return biomesMap.get(biome);
    }

    public ArrayList<BiomeLevel> getBiomeLevels() {
        return new ArrayList<>(biomesMap.values());
    }

    public boolean hasActiveRewards() {
        return !activeRewards.isEmpty();
    }

    public void addActiveReward(Reward reward) {
        activeRewards.add(reward);
    }

    public void clearActiveRewards() {
        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();

            for (Reward reward : getActiveRewards()) {
                if (reward instanceof PotionReward) {
                    ((PotionReward) reward).remove(onlinePlayer);
                }
                if (reward instanceof EffectReward) {
                    ((EffectReward) reward).remove(onlinePlayer);
                }
            }
        }

        activeRewards.clear();
    }

    public void clearActiveReward(Reward reward) {
        activeRewards.remove(reward);
    }
}
