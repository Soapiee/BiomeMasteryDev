package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.logic.rewards.EffectType;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;

public class EffectReward extends Reward {

    private final EffectType effect;

    public EffectReward(EffectType effect, boolean isTemporary) {
        super(RewardType.EFFECT, isTemporary);
        this.effect = effect;
    }

    @Override
    public void give(Player player) {
        //TODO
    }

    @Override
    public String toString() {
        return Utils.capitalise(effect.name()) + " effect";
    }
}
