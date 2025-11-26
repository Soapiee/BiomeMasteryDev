package me.soapiee.common.manager;

import com.zaxxer.hikari.pool.HikariPool;
import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.HikariCPConnection;
import me.soapiee.common.logic.ProgressChecker;
import me.soapiee.common.logic.rewards.RewardFactory;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class DataManager {

    @Getter private final ConfigManager configManager;
    @Getter private final BiomeDataManager biomeDataManager;
    @Getter private final PlayerDataManager playerDataManager;
    @Getter private final EffectsManager effectsManager;
    @Getter private PendingRewardsManager pendingRewardsManager;
    @Getter private CommandCooldownManager cooldownManager;
    @Getter private HikariCPConnection database;

    private ProgressChecker progressChecker;

    public DataManager(BiomeMastery main, boolean debugMode) {
        FileConfiguration mainConfig = main.getConfig();
        Logger logger = main.getCustomLogger();

        playerDataManager = new PlayerDataManager();
        effectsManager = new EffectsManager(main);
        RewardFactory rewardFactory = new RewardFactory(main, playerDataManager, effectsManager);
        configManager = new ConfigManager(mainConfig, rewardFactory, logger);
        biomeDataManager = new BiomeDataManager(configManager, rewardFactory, mainConfig, debugMode);
        cooldownManager = new CommandCooldownManager(main, mainConfig.getInt("settings.command_cooldown", 3));
        pendingRewardsManager = new PendingRewardsManager(main, biomeDataManager);
    }

    public void initialise(BiomeMastery main) throws IOException {
        if (configManager.isDatabaseEnabled()) {
            try {
                initialiseDatabase(main.getConfig());
                Utils.consoleMsg(ChatColor.DARK_GREEN + "Database enabled.");
            } catch (SQLException | HikariPool.PoolInitializationException e) {
                main.getCustomLogger().logToFile(e, ChatColor.RED + "Database could not connect. Switching to file storage");
                initialiseFiles(main);
            }

        } else {
            initialiseFiles(main);
        }
    }

    private void initialiseDatabase(FileConfiguration config) throws SQLException, IOException {
        database = new HikariCPConnection(config);
        database.connect(configManager.getEnabledBiomes());
    }

    public void initialiseFiles(BiomeMastery main) throws IOException {
        configManager.setDatabaseEnabled(false);
        database = null;

        Files.createDirectories(Paths.get(main.getDataFolder() + File.separator + "Data" + File.separator + "BiomeLevels"));
        Utils.consoleMsg(ChatColor.DARK_GREEN + "File Storage enabled.");
    }

    public void reloadData(BiomeMastery main, DataManager dataManager) {
        configManager.reload(main, dataManager);
        startChecker(main);
    }

    public void startChecker(BiomeMastery main) {
        if (progressChecker != null)
            try {
                progressChecker.cancel();
            } catch (IllegalStateException ignored) {
            }

        progressChecker = new ProgressChecker(main);
    }

    public void saveAll(){
        playerDataManager.saveAll(false);
        cooldownManager.save();
        pendingRewardsManager.save();
    }

}
