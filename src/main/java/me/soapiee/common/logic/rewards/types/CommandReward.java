package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.logic.rewards.RewardType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class CommandReward extends Reward {

    private final ArrayList<String> commandList;

    public CommandReward(ArrayList<String> commandList) {
        super(RewardType.COMMAND, true);
        this.commandList = commandList;
    }

    @Override
    public void give(Player player) {
        for (String command : commandList) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

    @Override
    public String toString() {
//        StringBuilder builder = new StringBuilder();
//        int i = 1;
//
//        builder.append(type.toString().toLowerCase()).append("s: ");
//        for (String permission : commandList) {
//            builder.append(permission);
//            if (commandList.size() > i) builder.append(", ");
//            i++;
//        }
//
//        return builder.toString();

        //TODO Add reward decription section to config
        return "Custom commands";
    }
}
