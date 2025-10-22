package me.soapiee.common.data.rewards.types;

import lombok.AllArgsConstructor;
import me.soapiee.common.data.rewards.RewardType;

@AllArgsConstructor
public abstract class AbstractReward implements Reward {

    protected RewardType type;

}
