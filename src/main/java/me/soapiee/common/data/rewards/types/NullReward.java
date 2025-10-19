package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;

public class NullReward extends Reward {

    public NullReward() {
        super(RewardType.NONE);
    }

    @Override
    public void give(Player player) {
    }

    @Override
    public String toString() {
        return "No reward";
    }
}
