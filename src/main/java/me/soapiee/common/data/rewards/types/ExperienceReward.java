package me.soapiee.common.data.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;

public class ExperienceReward extends Reward {

    private final int amount;

    public ExperienceReward(BiomeMastery main, int amount) {
        super(main, RewardType.EXPERIENCE);
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
