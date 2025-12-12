package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;

public class ExperienceReward extends Reward {

    private final int amount;

    public ExperienceReward(BiomeMastery main, int amount) {
        super(RewardType.EXPERIENCE, true, main.getMessageManager());
        this.amount = amount;
    }

    @Override
    public void give(Player player) {
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDRECEIVED, toString())));
        player.giveExpLevels(amount);
    }

    @Override
    public String toString() {
        return amount + " exp level" + (amount != 1 ? "s" : "");
    }
}
