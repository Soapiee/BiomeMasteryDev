package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;

public abstract class Reward implements RewardInterface {

    protected RewardType type;

    public Reward(RewardType type) {
        this.type = type;
    }

    @Override
    public RewardType getType() {
        return this.type;
    }

//    public abstract void give(Player player);

//    public abstract String toString();

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
