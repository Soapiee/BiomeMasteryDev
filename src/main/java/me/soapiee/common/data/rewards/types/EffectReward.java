package me.soapiee.common.data.rewards.types;

import me.soapiee.common.data.rewards.EffectType;
import me.soapiee.common.data.rewards.RewardType;

public class EffectReward extends AbstractReward {

    private final EffectType effect;

    public EffectReward(EffectType effect) {
        super(RewardType.EFFECT);
        this.effect = effect;
    }

    @Override
    public String toString() {
        return effect.name() + " effect";
    }

}
