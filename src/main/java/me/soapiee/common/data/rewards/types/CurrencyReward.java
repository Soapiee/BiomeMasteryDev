package me.soapiee.common.data.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.RewardType;
import me.soapiee.common.hooks.VaultHook;
import org.bukkit.entity.Player;

public class CurrencyReward extends Reward {

    private final VaultHook vaultHook;
    private final double amount;

    public CurrencyReward(BiomeMastery main, double amount) {
        super(main, RewardType.CURRENCY);
        this.vaultHook = main.getVaultHook();
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
