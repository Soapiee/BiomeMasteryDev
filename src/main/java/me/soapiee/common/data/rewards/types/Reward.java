package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;

public abstract class Reward {

    RewardType type;

    public Reward(RewardType type) {
        this.type = type;
    }

    public RewardType getType() {
        return this.type;
    }

    public abstract void give(Player player);

    public abstract String toString();

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
