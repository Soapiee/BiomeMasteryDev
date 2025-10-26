package me.soapiee.common.logic.rewards.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.soapiee.common.logic.rewards.RewardType;

@AllArgsConstructor
public abstract class Reward implements RewardInterface {

    @Getter protected final RewardType type;
    @Getter private final boolean isTemporary;

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
