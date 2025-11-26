package me.soapiee.common.logic.rewards;

import org.bukkit.entity.Player;

public interface RewardInterface {

    default void give(Player player){}

    String toString();
}
