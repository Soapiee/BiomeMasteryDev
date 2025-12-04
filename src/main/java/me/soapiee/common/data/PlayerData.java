package me.soapiee.common.data;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.types.EffectReward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.manager.ConfigManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerData {

    @Getter private final OfflinePlayer player;
    @Getter private final Map<Biome, BiomeLevel> biomesMap = new HashMap<>();
    @Getter private final ArrayList<Reward> activeRewards = new ArrayList<>();
    private final PlayerStorageHandler storageHandler;

    public PlayerData(BiomeMastery main, @NotNull OfflinePlayer player) throws IOException, SQLException {
        this.player = player;

        ConfigManager configManager = main.getDataManager().getConfigManager();
        if (configManager.isDatabaseEnabled()) storageHandler = new PlayerDatabaseStorage(main, this);
        else storageHandler = new PlayerFileStorage(main, this);

        storageHandler.readData();
    }

    public void saveData(boolean async) {
        storageHandler.saveData(async);
    }

    public BiomeLevel getBiomeLevel(Biome biome) {
        return biomesMap.get(biome);
    }

    public ArrayList<BiomeLevel> getBiomeLevels() {
        return new ArrayList<>(biomesMap.values());
    }

    public boolean hasActiveRewards() {
        return !activeRewards.isEmpty();
    }

    public void addActiveReward(Reward reward) {
        activeRewards.add(reward);
    }

    public void clearActiveRewards() {
        if (!player.isOnline()) return;

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) return;

        ArrayList<Reward> copyActiveRewards = new ArrayList<>(getActiveRewards());

        for (Reward reward : copyActiveRewards) {
            if (reward instanceof PotionReward) {
                ((PotionReward) reward).remove(onlinePlayer);
            }
            if (reward instanceof EffectReward) {
                ((EffectReward) reward).remove(onlinePlayer);
            }
        }
    }

    public void clearActiveReward(Reward reward) {
        activeRewards.remove(reward);
    }
}
