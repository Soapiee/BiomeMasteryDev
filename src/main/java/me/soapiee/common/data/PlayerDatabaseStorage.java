package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.*;
import me.soapiee.common.manager.BiomeDataManager;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.DataManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDatabaseStorage implements PlayerStorageHandler{

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final MessageManager messageManager;
    private final PlayerData playerData;
    private final Logger logger;

    private final UUID uuid;
    private final OfflinePlayer player;

    public PlayerDatabaseStorage(BiomeMastery main, PlayerData playerData) {
        this.main = main;
        dataManager = main.getDataManager();
        configManager = main.getDataManager().getConfigManager();
        biomeDataManager = main.getDataManager().getBiomeDataManager();
        messageManager = main.getMessageManager();
        this.playerData = playerData;
        logger = main.getCustomLogger();
        uuid = playerData.getPlayer().getUniqueId();
        player = playerData.getPlayer();

        createPlayerLevels();
    }

//    private void createPlayerLevels(){
//        for (Biome key : configManager.getEnabledBiomes()) {
//            playerData.getBiomesMap().put(key, new BiomeLevel(player, biomeDataManager.getBiomeData(key)));
//        }
//    }
//
//    @Override
//    public void readData() {
//        for (Biome key : configManager.getEnabledBiomes()) {
//            getPlayerData(key.name(), (player2, results, error) -> {
//                if (error != null) {
//                    logger.logToPlayer((CommandSender) player2, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
//                    return;
//                }
//
//                playerData.getBiomesMap().get(key).setLevel(results.getLevel());
//                playerData.getBiomesMap().get(key).setProgress(results.getProgress());
//            });
//        }
//    }

    @Override
    public void saveData(boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                for (final Biome biome : playerData.getBiomesMap().keySet()) {
                    String biomeName = biome.name();
                    try (Connection connection = dataManager.getDatabase().getDatabase().getConnection();
                         PreparedStatement statement = connection.prepareStatement("UPDATE " + biomeName + " SET LEVEL=?, PROGRESS=? WHERE UUID=?;")) {
                        statement.setInt(1, playerData.getBiomeLevel(biome).getLevel());
                        statement.setLong(2, playerData.getBiomeLevel(biome).getProgress());
                        statement.setString(3, uuid.toString());
                        statement.executeUpdate();

                    } catch (SQLException e) {
                        logger.logToFile(e, ChatColor.RED + player.getName() + "'s " + biomeName + " data could not be saved");
                    }
                }
            });

        } else {
            for (final Biome biome : configManager.getEnabledBiomes()) {
                String biomeName = biome.name();
                try (Connection connection = dataManager.getDatabase().getDatabase().getConnection();
                     PreparedStatement statement = connection.prepareStatement("UPDATE " + biomeName + " SET LEVEL=?, PROGRESS=? WHERE UUID=?;")) {
                    statement.setInt(1, playerData.getBiomeLevel(biome).getLevel());
                    statement.setLong(2, playerData.getBiomeLevel(biome).getProgress());
                    statement.setString(3, uuid.toString());
                    statement.executeUpdate();

                } catch (SQLException e) {
                    logger.logToFile(e, ChatColor.RED + player.getName() + "'s " + biomeName + " data could not be saved");
                }
            }
        }
    }

    private void getPlayerData(final String table, final Callback<BiomeLevel> callback) {
        //The tables are named after biome enums
        final BiomeData biomeData = biomeDataManager.getBiomeData(table);

        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {

            //Check it exists, and if not, create entry
            try (Connection connection = dataManager.getDatabase().getDatabase().getConnection();
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


//    =-=-=-=-=-=-=-=-=-=-=-=-= BIOME DATA POST GROUP UPDATE =-=-=-=-=-=-=-=-=-=-=-=-=
    @Override
    public void readData() {
        for (BiomeData biomeData : biomeDataManager.getBiomeDataMap().values()) {
            if (biomeData instanceof ChildData) continue;

            Biome key = biomeData.getBiome();
            getPlayerData(key.name(), (player2, results, error) -> {
                if (error != null) {
                    logger.logToPlayer((CommandSender) player2, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                    return;
                }

                playerData.getBiomesMap().get(key).setLevel(results.getLevel());
                playerData.getBiomesMap().get(key).initialiseProgress(results.getProgress());
            });
        }
    }

    private void createPlayerLevels(){
        if (configManager.isBiomesGrouped()){
            for (Biome parentBiome : configManager.getParentAndChildrenMap().keySet()) {
                playerData.getBiomesMap().put(parentBiome, new BiomeLevel(player, biomeDataManager.getBiomeData(parentBiome)));

                if (configManager.isDebugMode()) Utils.debugMsg(player.getName(),
                        ChatColor.GREEN + parentBiome.name() + " level setup");
            }
        }

        for (Biome enabledBiome : configManager.getEnabledBiomes()){
            if (playerData.getBiomesMap().containsKey(enabledBiome)) continue;

            BiomeData biomeData = biomeDataManager.getBiomeData(enabledBiome);
            BiomeLevel level;

            if (biomeData instanceof SingularData){
                level = new BiomeLevel(player, biomeData);
            } else {
                level = playerData.getBiomesMap().get(biomeData.getBiome());
            }

            if (configManager.isDebugMode()) Utils.debugMsg(player.getName(),
                    ChatColor.GREEN + enabledBiome.name() + " level setup");
            playerData.getBiomesMap().put(enabledBiome, level);

        }
    }

}
