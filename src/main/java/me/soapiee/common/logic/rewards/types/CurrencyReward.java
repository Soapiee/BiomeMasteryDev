package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;

public class CurrencyReward extends Reward {

    private final VaultHook vaultHook;
    private final double amount;

    public CurrencyReward(BiomeMastery main, double amount) {
        super(RewardType.CURRENCY, true, main.getMessageManager());
        this.vaultHook = main.getVaultHook();
        this.amount = amount;
    }

    @Override
    public void give(Player player) {
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDRECEIVED, toString())));
        vaultHook.deposit(player, amount);
    }

    @Override
    public String toString() {
        return amount + vaultHook.getCurrencyName();
    }
}
