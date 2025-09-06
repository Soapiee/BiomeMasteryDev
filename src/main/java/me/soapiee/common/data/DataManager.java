package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import javax.naming.CommunicationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class DataManager {

    private final BiomeMastery main;
    private final HikariCPConnection database;
    private final Logger logger;
    private final String dataSaveType;

    private final HashMap<UUID, PlayerData> dataMap;
    private int targetTime;

    public DataManager(BiomeMastery main) throws IOException, SQLException, CommunicationException {
        this.main = main;
        logger = main.getCustomLogger();
        dataMap = new HashMap<>();

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

        readVariables();
    }

    public void reload() {
        readVariables();
    }

    private void readVariables() {
        FileConfiguration config = main.getConfig();

        targetTime = config.getInt("target_duration.target_time");
    }

    public void add(PlayerData data) {
        dataMap.put(data.uuid, data);
    }

    public void remove(UUID uuid) {
        dataMap.remove(uuid);
    }

    public boolean has(UUID uuid) {
        return dataMap.containsKey(uuid);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return dataMap.get(uuid);
    }

    public String getDataSaveType() {
        return dataSaveType;
    }

    public HikariCPConnection getDatabase() {
        return database;
    }

    public int getTargetTime() {
        return targetTime;
    }

    public void saveAll(boolean async) {
        for (UUID uuid : dataMap.keySet()) {
            getPlayerData(uuid).saveData(async);
        }
    }
}
