package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.manager.ConfigManager;
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

    public ProgressChecker(BiomeMastery main) {
        playerDataManager = main.getDataManager().getPlayerDataManager();
        configManager = main.getDataManager().getConfigManager();

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

            playerLevel.updateProgress();
        }
    }
}
