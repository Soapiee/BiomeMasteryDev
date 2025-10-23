package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.util.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Progress extends BukkitRunnable {

    private final PlayerCache playerCache;
    private final DataManager dataManager;
    private final int progressSeconds;

    public Progress(BiomeMastery main, long delay) {
        playerCache = main.getPlayerCache();
        dataManager = main.getDataManager();
        progressSeconds = (int) delay;
        runTaskTimer(main, 0, delay * 60);
    }

    @Override
    public void run() {
        // Get a list of all players

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = dataManager.getPlayerData(player.getUniqueId());

            Biome currentBiome = player.getLocation().getBlock().getBiome();

            data.getBiomeLevel(currentBiome).addProgress();
        }
        // Loop the list and add 1 minute progress
    }
}
