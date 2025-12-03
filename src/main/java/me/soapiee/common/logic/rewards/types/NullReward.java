package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardType;

public class NullReward extends Reward {

    public NullReward() {
        super(RewardType.NONE, true, null);
    }

    @Override
    public String toString() {
        return "No reward";
    }
}
