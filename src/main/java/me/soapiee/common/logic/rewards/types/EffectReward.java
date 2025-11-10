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

        //TODO Add persistent data key
    }

    public void remove(Player player){
        //TODO

        //TODO Remove persistent data key
    }

    public EffectType getReward() {
        //TODO Get persistent data key
        return effect;
    }

    @Override
    public String toString() {
        return Utils.capitalise(effect.name()) + " effect";
    }
}
