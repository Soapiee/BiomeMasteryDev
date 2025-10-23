package me.soapiee.common.data.rewards.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.soapiee.common.data.rewards.RewardType;

@AllArgsConstructor
public abstract class Reward implements RewardInterface {

    @Getter protected RewardType type;

    //    public boolean equals(Object comparedObject) {
//        if (this == comparedObject) return true;
//
//        if (!(comparedObject instanceof Reward)) return false;
//
//        Reward comparedReward = (Reward) comparedObject;
//
//        return this.type.equals(comparedReward.type);
//    }
}
