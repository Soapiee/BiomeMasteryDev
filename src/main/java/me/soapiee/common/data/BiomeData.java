package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class BiomeData {

    private final Biome biome;
    private final HashMap<Integer, Integer> levels;
    //private final HashMap<Integer, Reward> rewards;

    public BiomeData(BiomeMastery main, Biome biome, boolean isDefault) {
        this.biome = biome;
        levels = new HashMap<>();
//        rewards = new HashMap<>();

        FileConfiguration config = main.getConfig();

        if (isDefault) {
            for (String key : config.getConfigurationSection("default_settings.levels").getKeys(false)) {
                levels = main.getDataManager().getDefaultLevels;
//                rewards = main.getDataManager().getDefaultRewards;
            }
        } else {
            String biomeName = biome.name();
            for (String key : config.getConfigurationSection("Biomes." + biomeName).getKeys(false)) {
                int level = Integer.parseInt(key);
                levels.put(level, config.getInt("Biomes." + biomeName + "." + level + ".target_duration"));
//            rewards.put(level, new Reward());
            }
        }

    }

    public int getTargetDuration(int level) {
        return levels.get(level);
    }

//    public Reward getReward(int level){
//        return rewards.get(level);
//    }
}
