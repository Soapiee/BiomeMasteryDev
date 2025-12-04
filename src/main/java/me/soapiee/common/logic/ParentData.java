package me.soapiee.common.logic;

import lombok.Getter;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardFactory;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.util.Utils;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParentData implements BiomeData {

    @Getter private final Biome biome;
    @Getter private final String biomeName;

    private final HashMap<Integer, Integer> levels;
    private final HashMap<Integer, Reward> rewards;

    @Getter private final ArrayList<Biome> children;

    public ParentData(ConfigManager configManager,
                      RewardFactory rewardFactory,
                      FileConfiguration config,
                      Biome biome) {
        this.biome = biome;
        biomeName = getGroupName(config);
        levels = new HashMap<>();
        rewards = new HashMap<>();
        children = configManager.getParentAndChildrenMap().get(biome);

        setRewards(configManager, rewardFactory, config);
    }

    private String getGroupName(FileConfiguration config){
        for (String groupName : config.getConfigurationSection("groups").getKeys(false)){
            List<String> biomeGroup = config.getStringList("groups." + groupName);

            if (biomeGroup.contains(biome.name())) return groupName;
        }

        return null;
    }

    @Override
    public int getTargetDuration(int level) {
        return levels.getOrDefault(level + 1, 0);
    }

    @Override
    public Reward getReward(int level) {
        return rewards.get(level);
    }

    @Override
    public int getMaxLevel() {
        int max = 0;

        for (Integer level : levels.keySet()) {
            if (level >= max) max = level;
        }

        return max;
    }

    private void setRewards(ConfigManager configManager, RewardFactory rewardFactory, FileConfiguration config) {
        boolean isDefault = config.getConfigurationSection("biomes." + biome.name()) == null;
        if (configManager.isDebugMode()) Utils.debugMsg("", biome.name() + "&6 is the parent of group: " + biomeName + "(default: " + isDefault + ")");

        if (isDefault) {
            levels.putAll(configManager.getDefaultLevelsThresholds());
            rewards.putAll(configManager.getDefaultRewards());

        } else {
            String biomeName = biome.name();

            for (String key : config.getConfigurationSection("biomes." + biomeName).getKeys(false)) {
                int level = Integer.parseInt(key);
                levels.put(level, config.getInt("biomes." + biomeName + "." + level + ".target_duration"));
                rewards.put(level, rewardFactory.create("biomes." + biomeName + "." + level + "."));
            }
        }
    }
}
