package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PermissionReward extends Reward {

    private final VaultHook vaultHook;
    private final ArrayList<String> permissions;

    public PermissionReward(BiomeMastery main, ArrayList<String> permission) {
        super(RewardType.PERMISSION, true, main.getMessageManager());
        this.vaultHook = main.getVaultHook();
        this.permissions = permission;
    }

    @Override
    public void give(Player player) {
        for (String permission : permissions) {
            vaultHook.setPermission(player, permission);
        }

        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDACTIVATED, toString())));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 1;

        builder.append(type.toString().toLowerCase()).append("s: ");
        for (String permission : permissions) {
            builder.append(permission);
            if (permissions.size() > i) builder.append(", ");
            i++;
        }

        return builder.toString();
    }
}
