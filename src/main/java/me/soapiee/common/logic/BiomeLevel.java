package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.BiomeData;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.logic.events.LevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;

public class BiomeLevel {

    private final DataManager dataManager;
    private final OfflinePlayer player;

    private int level;
    private int progress;
    private LocalDateTime entryTime;

    public BiomeLevel(BiomeMastery main, OfflinePlayer player, BiomeData biomeData, int level, int progress) throws NullPointerException {
        dataManager = main.getDataManager();
        this.player = player;
        this.level = level;
        this.progress = progress;

        if (player == null) throw new NullPointerException("Player is null");
    }

    public BiomeLevel(BiomeMastery main, OfflinePlayer player, BiomeData biomeData) {
        this(main, player, biomeData, 0, 0);
    }

    public int getLevel() {
        return level;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public int getProgress() {
        return progress;
    }

    public void setEntryTime(LocalDateTime newEntryTime) {
        entryTime = newEntryTime;
    }

    public void addProgress(int seconds) {
        progress += seconds;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int targetTime = dataManager.getBiomeData(Biome).getTargetTime();
        if (progress < targetTime) return;

        while (progress >= targetTime) {
            level++;
            progress -= targetTime;
//            Utils.consoleMsg(Utils.colour("&eedited progress: " + progress));
            LevelUpEvent event = new LevelUpEvent(player, level);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

}
