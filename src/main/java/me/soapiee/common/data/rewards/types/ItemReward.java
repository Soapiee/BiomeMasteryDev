package me.soapiee.common.data.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.RewardType;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ItemReward extends Reward {

    private final ArrayList<ItemStack> itemList;

    public ItemReward(BiomeMastery main, ArrayList<ItemStack> itemList) {
        super(main, RewardType.ITEM);
        this.itemList = itemList;
    }

    @Override
    public void give(Player player) {
        for (ItemStack item : itemList) {
            if (Utils.hasFreeSpace(item.getType(), item.getAmount(), player)) {
                player.getInventory().addItem(item);
            } else {
                player.getLocation().getWorld().dropItem(player.getLocation(), item);
//                        player.sendMessage(Utils.colour(messageManager.get(Message.GAMEITEMWINERROR)));
                return;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 1;

        for (ItemStack item : itemList) {
            builder.append(item.getAmount()).append(" ").append(item.getType().toString().toLowerCase().replace("_", " "));
            if (itemList.size() > i) builder.append(", ");
            i++;
        }

        return builder.toString();
    }
}
