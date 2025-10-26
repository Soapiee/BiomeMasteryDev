package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.logic.rewards.RewardType;

public class NullReward extends Reward {

    public NullReward() {
        super(RewardType.NONE, true);
    }

    @Override
    public String toString() {
        return "No reward";
    }
}
