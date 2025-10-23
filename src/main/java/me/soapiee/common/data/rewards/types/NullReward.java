package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;

public class NullReward extends Reward {

    public NullReward() {
        super(RewardType.NONE);
    }

    @Override
    public String toString() {
        return "No reward";
    }
}
