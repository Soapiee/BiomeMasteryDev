package me.soapiee.common.data.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;

public class NullReward extends Reward {

    public NullReward(BiomeMastery main) {
        super(main, RewardType.NONE);
    }

    @Override
    public void give(Player player) {
    }

    @Override
    public String toString() {
        return "";
    }
}
