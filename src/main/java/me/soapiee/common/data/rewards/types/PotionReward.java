package me.soapiee.common.data.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionReward extends Reward {

    private final PotionEffect potion;

    public PotionReward(BiomeMastery main, PotionEffectType potionType, int amplifier) {
        super(main, RewardType.POTION);
        this.potion = new PotionEffect(potionType, Integer.MAX_VALUE, amplifier);
    }

    @Override
    public void give(Player player) {
        player.addPotionEffect(potion);

        //add them to a list that checks when they leave the biome - so the effect can be removed
    }

    @Override
    public String toString() {
        return "";
    }
}
