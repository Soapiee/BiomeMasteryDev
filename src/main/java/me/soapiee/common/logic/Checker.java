package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Checker extends BukkitRunnable {

    private final DataManager dataManager;
    private final List<String> enabledWorlds;

    public Checker(BiomeMastery main, long delay) {
        dataManager = main.getDataManager();
        enabledWorlds = dataManager.getEnabledWorlds();
        runTaskTimer(main, 0, delay * 20);
    }

    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location location = player.getLocation();

            //Check player is in an enabled world
            if (!enabledWorlds.contains(location.getWorld().getName())) return;

            //Check player is in an enabled biome
            Biome playerBiome = location.getBlock().getBiome();
            if (!enabledWorlds.contains(playerBiome.toString())) return;

            BiomeLevel playerLevel = dataManager.getPlayerData(player.getUniqueId()).getBiomeData(playerBiome);
            int currentProgress = playerLevel.getProgress();
            //int totalProgress = difference between: playerLevel.getEntryTime() + LocalDateTime.now();
            if (//totalProgress is more than biome targetTime){
            playerLevel.addProgress(totalProgress);
        }

    }
    // Get a list of all players
    // Loop the list and check their current progress value
    // If progress is above threshold, level them up
}
}
