package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.manager.BiomeDataManager;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.DataManager;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ProgressChecker extends BukkitRunnable {

    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;

    public ProgressChecker(BiomeMastery main, DataManager dataManager) {
        playerDataManager = dataManager.getPlayerDataManager();
        configManager = dataManager.getConfigManager();
        biomeDataManager = dataManager.getBiomeDataManager();

        long delay = configManager.getUpdateInterval();
        if (configManager.isDebugMode())
            Utils.debugMsg("", ChatColor.YELLOW + "Progress Checker started with a " + delay + " second delay");

        runTaskTimer(main, 0, delay * 20);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Biome playerBiome = player.getLocation().getBlock().getBiome();

            if (!configManager.isEnabledBiome(playerBiome)) continue;
            if (!configManager.isEnabledWorld(player.getWorld())) continue;

            BiomeLevel playerLevel = playerDataManager.getPlayerData(player.getUniqueId()).getBiomeLevel(playerBiome);
            if (playerLevel.isMaxLevel()) continue;

            playerLevel.updateProgress(playerBiome);
        }
    }
}
