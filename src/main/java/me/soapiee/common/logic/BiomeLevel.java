package me.soapiee.common.logic;

import lombok.Getter;
import lombok.Setter;
import me.soapiee.common.data.BiomeData;
import me.soapiee.common.logic.events.LevelUpEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class BiomeLevel {

    private final OfflinePlayer player;

    private final BiomeData biomeData;
    @Getter private int level;
    @Getter private int progress;
    @Getter @Setter private LocalDateTime entryTime;

    public BiomeLevel(@NotNull OfflinePlayer player, BiomeData biomeData, int level, int progress) throws NullPointerException {
        this.player = player;
        this.biomeData = biomeData;
        this.level = level;
        this.progress = progress;

//        if (player == null) throw new NullPointerException("Player is null");
    }

    public BiomeLevel(OfflinePlayer player, BiomeData biomeData) {
        this(player, biomeData, 0, 0);
    }

    public void clearEntryTime() {
        entryTime = null;
    }

    public void addProgress() {
        if (getEntryTime() == null) return;

        int toAdd = (int) ChronoUnit.SECONDS.between(entryTime, LocalDateTime.now());
        entryTime = LocalDateTime.now();

        progress += toAdd;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int targetTime = biomeData.getTargetDuration(level);
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
