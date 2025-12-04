package me.soapiee.common.logic;

import lombok.Getter;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.util.Utils;
import org.bukkit.block.Biome;

public class ChildData implements BiomeData{

    @Getter private final Biome biome;
    @Getter private final String biomeName;

    @Getter private final BiomeData parentData;

    public ChildData(Biome childBiome, BiomeData parentData, boolean debugMode) {
        this.biome = childBiome;
        biomeName = biome.name();
        this.parentData = parentData;
        if (debugMode) Utils.debugMsg("", biomeName + " is a child in the group: " + this.parentData.getBiomeName());
    }

    @Override
    public int getTargetDuration(int level) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Reward getReward(int level) {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return Integer.MAX_VALUE;
    }
}
