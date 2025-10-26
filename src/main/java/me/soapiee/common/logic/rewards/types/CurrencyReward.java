package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.hooks.VaultHook;
import org.bukkit.entity.Player;

public class CurrencyReward extends Reward {

    private final VaultHook vaultHook;
    private final double amount;

    public CurrencyReward(VaultHook vaultHook, double amount) {
        super(RewardType.CURRENCY, true);
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
