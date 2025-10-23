package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ItemReward extends Reward {

    private final MessageManager messageManager;
    private final ArrayList<ItemStack> itemList;

    public ItemReward(MessageManager messageManager, ArrayList<ItemStack> itemList) {
        super(RewardType.ITEM);
        this.messageManager = messageManager;
        this.itemList = itemList;
    }

    public ItemStack getReward(int index) {
        return itemList.get(index);
    }

    @Override
    public void give(Player player) {
        for (ItemStack item : itemList) {
            if (Utils.hasFreeSpace(item.getType(), item.getAmount(), player)) {
                player.getInventory().addItem(item);
            } else {
                player.getLocation().getWorld().dropItem(player.getLocation(), item);
                player.sendMessage(Utils.colour(messageManager.get(Message.INVFULL)));
                return;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 1;

        builder.append(type.toString().toLowerCase()).append("s: ");
        for (ItemStack item : itemList) {
            builder.append(item.getAmount()).append(" ").append(item.getType().toString().toLowerCase().replace("_", " "));
            if (itemList.size() > i) builder.append(", ");
            i++;
        }

        return builder.toString();
    }
}
