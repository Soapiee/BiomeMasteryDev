package me.soapiee.common.data;

import lombok.Getter;
import me.soapiee.common.data.rewards.types.Reward;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class BiomeData {

    @Getter private final Biome biome;
    private final HashMap<Integer, Integer> levels;
    private final HashMap<Integer, Reward> rewards;

    public BiomeData(DataManager dataManager,
                     FileConfiguration config,
                     Biome biome,
                     boolean isDefault,
                     CommandSender sender) {
        this.biome = biome;
        levels = new HashMap<>();
        rewards = new HashMap<>();

        if (isDefault) {
            levels.putAll(dataManager.getDefaultLevels());
            rewards.putAll(dataManager.getDefaultRewards());

        } else {
            String biomeName = biome.name();

            for (String key : config.getConfigurationSection("biomes." + biomeName).getKeys(false)) {
                int level = Integer.parseInt(key);
                levels.put(level, config.getInt("biomes." + biomeName + "." + level + ".target_duration"));
                rewards.put(level, dataManager.createReward(sender, "biomes." + biomeName + "." + level + "."));
            }
        }
    }

    public int getTargetDuration(int level) {
        return levels.get(level);
    }

    public Reward getReward(int level) {
        return rewards.get(level);
    }
}
