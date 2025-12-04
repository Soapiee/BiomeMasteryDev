package me.soapiee.common.manager;

import lombok.Getter;
import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.rewards.RewardFactory;
import me.soapiee.common.util.Utils;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BiomeDataManager {

    private final ConfigManager configManager;
    private final RewardFactory rewardFactory;
    private final FileConfiguration config;
    private final boolean isDebugMode;

    @Getter private final Map<Biome, BiomeData> biomeDataMap = new ConcurrentHashMap<>();

    public BiomeDataManager(ConfigManager configManager,
                            RewardFactory rewardFactory,
                            FileConfiguration config) {
        this.configManager = configManager;
        this.rewardFactory = rewardFactory;
        this.config = config;
        this.isDebugMode = configManager.isDebugMode();
        createAllBiomeData();
    }

    private void createAllBiomeData() {
        for (Biome enabledBiome : configManager.getEnabledBiomes()) {
            createBiomeData(enabledBiome);
        }
    }

    private void createBiomeData(Biome biome) {
        if (isDebugMode) Utils.debugMsg("", "&eEnabled biome: " + biome.name());

        BiomeData biomeData = new BiomeData(configManager, rewardFactory, config, biome);
        biomeDataMap.put(biome, biomeData);
    }

    public BiomeData getBiomeData(Biome biome) {
        return biomeDataMap.getOrDefault(biome, null);
    }

    public BiomeData getBiomeData(String biome) throws IllegalArgumentException {
        return biomeDataMap.getOrDefault(Biome.valueOf(biome), null);
    }
}
