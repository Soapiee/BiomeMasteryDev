package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class PotionReward extends AbstractReward {

    private final PotionEffect potion;

    public PotionReward(PotionType potionType, int amplifier) {
        super(RewardType.POTION);
        this.potion = new PotionEffect(potionType.getEffectType(), Integer.MAX_VALUE, amplifier);
    }

    @Override
    public void give(Player player) {
        player.addPotionEffect(potion);

        //add them to a list that checks when they leave the biome - so the effect can be removed
    }

    @Override
    public String toString() {
        return potion.getType() + " " + potion.getAmplifier();
    }

}
