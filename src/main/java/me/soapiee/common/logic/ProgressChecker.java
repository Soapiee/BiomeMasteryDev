package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ProgressChecker extends BukkitRunnable {

    private final DataManager dataManager;

    public ProgressChecker(BiomeMastery main, long delay) {
        dataManager = main.getDataManager();
        runTaskTimer(main, 0, delay * 20);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Biome playerBiome = player.getLocation().getBlock().getBiome();

            if (!dataManager.isEnabledBiome(playerBiome)) continue;
            if (!dataManager.isEnabledWorld(player.getWorld())) continue;

            BiomeLevel playerLevel = dataManager.getPlayerData(player.getUniqueId()).getBiomeLevel(playerBiome);
            if (playerLevel.isMaxLevel()) continue;

            // TODO: Prevent this from being edited in two places at once
            playerLevel.updateProgress();
        }
    }
}
