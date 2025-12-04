package me.soapiee.common.logic;

import me.soapiee.common.logic.rewards.Reward;
import org.bukkit.block.Biome;

public interface BiomeData {

    String getBiomeName();
    Biome getBiome();
    int getTargetDuration(int level);
    Reward getReward(int level);
    int getMaxLevel();
}
