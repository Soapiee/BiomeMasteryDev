package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionReward extends Reward {

    private final PotionEffect potion;

    public PotionReward(PotionType potionType, int amplifier, boolean isTemporary) {
        super(RewardType.POTION, isTemporary);
        this.potion = new PotionEffect(potionType.getEffectType(), Integer.MAX_VALUE, amplifier);
    }

    @Override
    public void give(Player player) {
        player.addPotionEffect(potion);
    }

    public void remove(Player player){
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
