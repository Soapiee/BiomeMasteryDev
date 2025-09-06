package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.BiomeLevel;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public final UUID uuid;
    public final OfflinePlayer player;
    public final Map<Biome, BiomeLevel> biomesMap;

    private File file;
    private YamlConfiguration contents;

    public PlayerData(BiomeMastery main, OfflinePlayer player) throws IOException, SQLException {
        this.main = main;
        dataManager = main.getDataManager();
        messageManager = main.getMessageManager();
        logger = main.getCustomLogger();
        uuid = player.getUniqueId();
        this.player = player;
        biomesMap = new ConcurrentHashMap<>();

        switch (dataManager.getDataSaveType()) {
            case "database":
                for (Biome key : Biome.values()) {
                    readDatabase(key.toString(), (player2, results, error) -> {
                        if (error != null) {
                            logger.logToPlayer((CommandSender) player2, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                            return;
                        }
                        biomesMap.put(key, results);
                    });
                }
                break;

            case "files":
                file = new File(main.getDataFolder() + File.separator + "playerData", uuid + ".yml");
                contents = new YamlConfiguration();

                readFile();
                break;
        }
    }

    private void readFile() {
        synchronized (fileLock) {
            if (!file.exists()) {
                createFile();
                return;
            }
            contents = YamlConfiguration.loadConfiguration(file);
        }

        for (Biome biome : Biome.values()) {
            int level = contents.getInt(biome + ".Level");
            int progress = contents.getInt(biome + ".Progress");
            BiomeLevel biomeLevel = new BiomeLevel(main, player, level, progress);

            biomesMap.put(biome, biomeLevel);
        }
    }

    private void createFile() {
        final String playerName = player.getName();
        final OfflinePlayer offlinePlayer = player;
        final HashSet<String> biomes = new HashSet<>();
        for (Biome biome : Biome.values()) {
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
                            biomesMap.put(Biome.valueOf(biome), new BiomeLevel(main, offlinePlayer));
                        }

                        localCopy.save(file);
                    } catch (IOException e) {
                        main.getCustomLogger().logToFile(e, "Could not create new data for " + playerName);
                    }
                }
            }
        }.runTaskAsynchronously(main);
    }

    private void saveFile(boolean async) {
        for (Biome biomeKey : Biome.values()) {
            final String biome = biomeKey.toString();
            final int level = getBiomeData(biomeKey).getLevel();
            final int progress = getBiomeData(biomeKey).getProgress();
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

    public void saveData(boolean async) {
        if (dataManager.getDataSaveType().equalsIgnoreCase("database")) saveDatabase(async);
        else saveFile(async);
    }

    private void readDatabase(final String table, final Callback<BiomeLevel> callback) {
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
                    biomeLevel = new BiomeLevel(main, player);
                    Bukkit.getScheduler().runTask(main, () -> callback.onQueryDone(player, biomeLevel, null));
                    return;
                }

                //Get data
                int level = results.getInt("LEVEL");
                int progress = results.getInt("PROGRESS");
                biomeLevel = new BiomeLevel(main, player, level, progress);

                Bukkit.getScheduler().runTask(main, () -> callback.onQueryDone(player, biomeLevel, null));
            } catch (SQLException e) {
                Bukkit.getScheduler().runTask(main, () -> callback.onQueryDone(player, null, e));
            }
        });
    }

    private void saveDatabase(boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                for (final Biome biome : Biome.values()) {
                    try (Connection connection = dataManager.getDatabase().getConnection().getConnection();
                         PreparedStatement statement = connection.prepareStatement("UPDATE " + biome.name() + " SET LEVEL=?, PROGRESS=? WHERE UUID=?;")) {
                        statement.setInt(1, getBiomeData(biome).getLevel());
                        statement.setInt(2, getBiomeData(biome).getProgress());
                        statement.setString(3, uuid.toString());
                        statement.executeUpdate();

                    } catch (SQLException e) {
                        logger.logToFile(e, ChatColor.RED + player.getName() + "'s " + biome.name() + " data could not be saved");
                    }
                }
            });

        } else {
            for (final Biome biome : Biome.values()) {
                try (Connection connection = dataManager.getDatabase().getConnection().getConnection();
                     PreparedStatement statement = connection.prepareStatement("UPDATE " + biome.name() + " SET LEVEL=?, PROGRESS=? WHERE UUID=?;")) {
                    statement.setInt(1, getBiomeData(biome).getLevel());
                    statement.setInt(2, getBiomeData(biome).getProgress());
                    statement.setString(3, uuid.toString());
                    statement.executeUpdate();

                } catch (SQLException e) {
                    logger.logToFile(e, ChatColor.RED + player.getName() + "'s " + biome.name() + " data could not be saved");
                }
            }
        }
    }

    public BiomeLevel getBiomeData(Biome biome) {
        return biomesMap.get(biome);
    }


}
