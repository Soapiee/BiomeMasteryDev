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

        BiomeData biomeData = new BiomeData(configManager, rewardFactory, config, biome);
        biomeDataMap.put(biome, biomeData);
    }

    public BiomeData getBiomeData(Biome biome) {
        return biomeDataMap.getOrDefault(biome, null);
    }

    public BiomeData getBiomeData(String biome) throws IllegalArgumentException {
        return biomeDataMap.getOrDefault(Biome.valueOf(biome), null);
    }

//    =-=-=-=-=-=-=-=-=-=-=-=-= BIOME DATA POST GROUP UPDATE =-=-=-=-=-=-=-=-=-=-=-=-=

//    public BiomeDataManager(ConfigManager configManager,
//                            RewardFactory rewardFactory,
//                            FileConfiguration config,
//                            boolean isDebugMode) {
//        this.configManager = configManager;
//        this.rewardFactory = rewardFactory;
//        this.config = config;
//        this.isDebugMode = isDebugMode;
//
//        if (!configManager.getGroupBiomes().isEmpty()) createGroupBiomeData();
//        createSingularBiomeData();
//    }
//
//    private void createGroupBiomeData() {
//        for (Biome parentBiome : configManager.getGroupBiomes().keySet()) {
//            biomeDataMap.put(parentBiome, new ParentBiome(configManager, rewardFactory, config, parentBiome));
//            // Make sure the BiomeData<ParentBiome> collects the list of its children from the ConfigManager (getGroupBiomes().get(parentBiome))
//
//            for (Biome childBiome : configManager.getGroupBiomes().get(parentBiome)){
//                biomeDataMap.put(childBiome, new ChildBiome(configManager, rewardFactory, config, childBiome, parentBiome));
//            }
//        }
//    }
//
//    private void createSingularBiomeData() {
//        for (Biome enabledBiome : configManager.getEnabledBiomes()) {
//            if (biomeDataMap.containsKey(enabledBiome)) return;
//            if (isDebugMode) Utils.debugMsg("", "&cBiome: " + enabledBiome.name() + " is part of a group");
//
//            biomeDataMap.put(enabledBiome, new SingularBiome(configManager, rewardFactory, config, enabledBiome));
//        }
//    }
//
//    public BiomeData getBiomeData(Biome biome) {
//        BiomeData biomeData = biomeDataMap.getOrDefault(biome, null);
//        if (biomeData.isChild()) return biomeDataMap.get(biome.getParent());
//        return biomeData;
//    }
//
//    public BiomeData getBiomeData(String biome) throws IllegalArgumentException {
//        BiomeData biomeData = biomeDataMap.getOrDefault(Biome.valueOf(biome), null);
//        if (biomeData.isChild) return biomeDataMap.get(biome.getParent());
//        return biomeData;
//    }
}
