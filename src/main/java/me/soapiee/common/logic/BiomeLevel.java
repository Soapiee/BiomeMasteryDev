package me.soapiee.common.logic;

import lombok.Getter;
import lombok.Setter;
import me.soapiee.common.logic.events.LevelUpEvent;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class BiomeLevel {

    private final OfflinePlayer player;
    private final Object lock = new Object();

    private final BiomeData biomeData;
    @Getter private int level;
    @Getter private long progress;
    @Getter
    @Setter
    private LocalDateTime entryTime;

    public BiomeLevel(@NotNull OfflinePlayer player, BiomeData biomeData, int level, int progress) {
        this.player = player;
        this.biomeData = biomeData;
        this.level = level;
        this.progress = progress;
    }

    public BiomeLevel(OfflinePlayer player, BiomeData biomeData) {
        this(player, biomeData, 0, 0);
    }

    public void clearEntryTime() {
        entryTime = null;
    }

    public synchronized void updateProgress() {
        synchronized (lock) {
            if (isMaxLevel()) return;

            Player onlineTarget = player.getPlayer();
            if (onlineTarget == null) return;

            if (getEntryTime() == null) return;
            Biome playerBiome = onlineTarget.getLocation().getBlock().getBiome();
            if (!playerBiome.name().equalsIgnoreCase(getBiome())) return;

            long toAdd = ChronoUnit.SECONDS.between(entryTime, LocalDateTime.now());
            entryTime = LocalDateTime.now();
            Utils.debugMsg(player.getName(),
                    ChatColor.GREEN.toString() + toAdd + " seconds added to biome " + biomeData.getBiome().name());

            progress += toAdd;
            checkLevelUp();
        }
    }

    private int checkLevelUp() {
        int targetTime = biomeData.getTargetDuration(level);
        if (progress < targetTime) return 0;

        while (progress >= targetTime) {
            level++;
            progress -= targetTime;

            LevelUpEvent event = new LevelUpEvent(player, level, this);
            Bukkit.getPluginManager().callEvent(event);

            if (biomeData.getMaxLevel() == level) {
                progress = 0;
                clearEntryTime();
                return 2;
            }

            targetTime = biomeData.getTargetDuration(level);
        }

        return 1;
    }

    public int setLevel(int newLevel) {
        int maxLevel = biomeData.getMaxLevel();
        if (newLevel > maxLevel) return -1;
        if (newLevel < 0) return -1;
        if (newLevel == level) return -1;

        synchronized (lock) {
            level = newLevel;
            progress = 0;
            if (entryTime != null) entryTime = LocalDateTime.now();

            return level;
        }
    }

    public long setProgress(long newProgress) {
        synchronized (lock) {
            if (newProgress < 0) return -1;

            if (entryTime != null) entryTime = LocalDateTime.now();

            progress = newProgress;
            if (checkLevelUp() == 2) return -2;
            return progress;
        }
    }

    public void reset() {
        level = 0;
        progress = 0;
        if (entryTime != null) entryTime = LocalDateTime.now();
    }

    public boolean isMaxLevel() {
        return level == biomeData.getMaxLevel();
    }

    public Reward getReward(int level) {
        return biomeData.getReward(level);
    }

    public String getBiome() {
        return Utils.capitalise(biomeData.getBiome().name());
    }
}
