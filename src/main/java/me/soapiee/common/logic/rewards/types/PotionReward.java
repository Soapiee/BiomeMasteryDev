package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionReward extends Reward {

    private final PotionEffect potion;
    private final PlayerDataManager playerDataManager;

    public PotionReward(PotionType potionType, int amplifier, boolean isTemporary, PlayerDataManager playerDataManager) {
        super(RewardType.POTION, isTemporary);
        potion = new PotionEffect(potionType.getEffectType(), Integer.MAX_VALUE, amplifier);
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void give(Player player) {
        playerDataManager.getPlayerData(player.getUniqueId()).addActiveReward(this);
        player.addPotionEffect(potion);
    }

    public void remove(Player player){
        playerDataManager.getPlayerData(player.getUniqueId()).clearActiveReward(this);
        player.removePotionEffect(potion.getType());
    }

    public PotionEffectType getReward() {
        return potion.getType();
    }

    @Override
    public String toString() {
        return Utils.capitalise(potion.getType().getName()) + " " + potion.getAmplifier();
    }
}
