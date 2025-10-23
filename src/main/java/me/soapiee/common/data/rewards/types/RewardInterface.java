package me.soapiee.common.data.rewards.types;

import org.bukkit.entity.Player;

public interface RewardInterface {

    default void give(Player player){}

    String toString();
}
