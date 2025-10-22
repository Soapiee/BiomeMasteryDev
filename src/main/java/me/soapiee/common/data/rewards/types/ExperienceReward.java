package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;

public class ExperienceReward extends AbstractReward {

    private final int amount;

    public ExperienceReward(int amount) {
        super(RewardType.EXPERIENCE);
        this.amount = amount;
    }

    @Override
    public void give(Player player) {
        player.giveExpLevels(amount);
    }

    @Override
    public String toString() {
        return amount + " exp";
    }

}
