package me.soapiee.common.data.rewards.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.soapiee.common.data.rewards.RewardType;

@Data
@AllArgsConstructor
public abstract class AbstractReward implements Reward {

    protected RewardType type;

}
