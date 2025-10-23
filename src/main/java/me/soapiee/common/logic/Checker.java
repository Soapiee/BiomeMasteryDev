package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Checker extends BukkitRunnable {

    private final DataManager dataManager;

    public Checker(BiomeMastery main, long delay) {
        dataManager = main.getDataManager();
        runTaskTimer(main, 0, delay * 20);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Biome playerBiome = player.getLocation().getBlock().getBiome();
            BiomeLevel playerLevel = dataManager.getPlayerData(player.getUniqueId()).getBiomeLevel(playerBiome);
            int maxLevel = dataManager.getBiomeData(playerBiome).getMaxLevel();

            if (playerLevel.getLevel() == maxLevel) return;
            if (!dataManager.playerInEnabledBiome(playerBiome)) return;
            if (!dataManager.playerInEnabledWorld(player.getWorld())) return;

            // TODO: Prevent this from being edited in two places at once
            playerLevel.addProgress();
        }
    }
}
