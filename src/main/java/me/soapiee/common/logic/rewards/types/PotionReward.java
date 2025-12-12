package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionReward extends Reward {

    private final PotionEffect potion;
    private final PlayerDataManager playerDataManager;

    public PotionReward(BiomeMastery main, PlayerDataManager playerDataManager, PotionType potionType, int amplifier, boolean isSingular) {
        super(RewardType.POTION, isSingular, main.getMessageManager());
        potion = new PotionEffect(potionType.getEffectType(), Integer.MAX_VALUE, amplifier);
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void give(Player player) {
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDACTIVATED, toString())));
        playerDataManager.getPlayerData(player.getUniqueId()).addActiveReward(this);
        player.addPotionEffect(potion);
    }

    public void remove(Player player){
        playerDataManager.getPlayerData(player.getUniqueId()).clearActiveReward(this);
        player.removePotionEffect(potion.getType());
    }

    public PotionEffectType getPotion() {
        return potion.getType();
    }

    @Override
    public String toString() {
        return Utils.capitalise(potion.getType().getName()) + " " + potion.getAmplifier();
    }
}
