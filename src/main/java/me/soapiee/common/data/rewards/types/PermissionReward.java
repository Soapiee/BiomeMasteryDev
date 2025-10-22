package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;
import me.soapiee.common.hooks.VaultHook;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PermissionReward extends AbstractReward {

    private final VaultHook vaultHook;
    private final ArrayList<String> permissions;

    public PermissionReward(VaultHook vaultHook, ArrayList<String> permission) {
        super(RewardType.PERMISSION);
        this.vaultHook = vaultHook;
        this.permissions = permission;
    }

    @Override
    public void give(Player player) {
        for (String permission : permissions) {
            vaultHook.setPermission(player, permission);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 1;

        builder.append(getType().toString().toLowerCase()).append("s: ");
        for (String permission : permissions) {
            builder.append(permission);
            if (permissions.size() > i) builder.append(", ");
            i++;
        }

        return builder.toString();
    }

}
