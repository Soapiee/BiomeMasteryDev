package me.soapiee.common.logic;

import me.soapiee.common.BiomeMastery;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;

public class BiomeLevel {

    private final int targetTime;
    private final OfflinePlayer player;

    private int level;
    private int progress;
    private LocalDateTime entryTime;

    public BiomeLevel(BiomeMastery main, OfflinePlayer player, int level, int progress) throws NullPointerException {
        targetTime = main.getDataManager().getTargetTime();
        this.player = player;
        this.level = level;
        this.progress = progress;

        if (player == null) throw new NullPointerException("Player is null");
    }

    public BiomeLevel(BiomeMastery main, OfflinePlayer player) {
        this(main, player, 0, 0);
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
