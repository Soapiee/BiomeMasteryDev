package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;
import me.soapiee.common.hooks.VaultHook;
import org.bukkit.entity.Player;

public class CurrencyReward extends AbstractReward {

    private final VaultHook vaultHook;
    private final double amount;

    public CurrencyReward(VaultHook vaultHook, double amount) {
        super(RewardType.CURRENCY);
        this.vaultHook = vaultHook;
        this.amount = amount;
    }

    @Override
    public void give(Player player) {
        vaultHook.deposit(player, amount);
    }

    @Override
    public String toString() {
        return amount + vaultHook.getCurrencyName();
    }

}
