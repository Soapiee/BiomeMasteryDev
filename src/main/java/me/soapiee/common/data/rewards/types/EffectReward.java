package me.soapiee.common.data.rewards.types;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.EffectType;
import me.soapiee.common.data.rewards.RewardType;
import org.bukkit.entity.Player;

public class EffectReward extends Reward {

    private final EffectType effect;

    public EffectReward(BiomeMastery main, EffectType effect) {
        super(main, RewardType.EFFECT);
        this.effect = effect;
    }

    @Override
    public void give(Player player) {

    }

    @Override
    public String toString() {
        return "";
    }
}
