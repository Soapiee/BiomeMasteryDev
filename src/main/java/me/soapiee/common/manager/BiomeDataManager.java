package me.soapiee.common.manager;

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

    private final Map<Biome, BiomeData> biomeDataMap = new ConcurrentHashMap<>();

    public BiomeDataManager(ConfigManager configManager,
                            RewardFactory rewardFactory,
                            FileConfiguration config,
                            boolean isDebugMode) {
        this.configManager = configManager;
        this.rewardFactory = rewardFactory;
        this.config = config;
        this.isDebugMode = isDebugMode;

        createAllBiomeData();
    }

    private void createAllBiomeData() {
        for (Biome enabledBiome : configManager.getEnabledBiomes()) {
            createBiomeData(enabledBiome);
        }
    }

    private void createBiomeData(Biome biome) {
        if (isDebugMode) Utils.debugMsg("", "&eEnabled biome: " + biome.name());

        boolean isDefault = config.getConfigurationSection("biomes." + biome.name()) == null;
        if (isDebugMode) Utils.debugMsg("", "&eIs default: " + isDefault);

        BiomeData biomeData = new BiomeData(configManager, rewardFactory, config, biome, isDefault);
        biomeDataMap.put(biome, biomeData);
    }

    public BiomeData getBiomeData(Biome biome) {
        return biomeDataMap.getOrDefault(biome, null);
    }

    public BiomeData getBiomeData(String biome) throws IllegalArgumentException {
        return biomeDataMap.getOrDefault(Biome.valueOf(biome), null);
    }
}
