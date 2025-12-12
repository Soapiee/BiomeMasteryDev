package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandReward extends Reward {

    private final ArrayList<String> commandList;
    private final String description;

    public CommandReward(BiomeMastery main, ArrayList<String> commandList, String description) {
        super(RewardType.COMMAND, true, main.getMessageManager());
        this.commandList = commandList;
        this.description = description;
    }

    @Override
    public void give(Player player) {
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDRECEIVED, toString())));
        for (String command : commandList) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

    @Override
    public String toString() {
        return description;
    }
}
