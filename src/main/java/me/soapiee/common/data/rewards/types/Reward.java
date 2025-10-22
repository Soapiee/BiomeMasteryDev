package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;

public interface Reward {

    RewardType getType();

    default void give(Player player) {
    }

    String toString();

}
