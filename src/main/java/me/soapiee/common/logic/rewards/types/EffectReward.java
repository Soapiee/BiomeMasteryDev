package me.soapiee.common.logic.rewards.types;

import me.soapiee.common.logic.effects.EffectType;
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
        //TODO Give effect

        //TODO Add persistent data key
//        playerDataManager.getPlayerData(player.getUniqueId()).addActiveReward(this);
    }

    public void remove(Player player){
        //TODO Remove effect

        //TODO Remove persistent data key
//        playerDataManager.getPlayerData(player.getUniqueId()).clearActiveReward(this);
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
