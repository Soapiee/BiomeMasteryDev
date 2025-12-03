package me.soapiee.common.logic.rewards.types;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.effects.Effect;
import me.soapiee.common.logic.effects.EffectType;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.manager.EffectsManager;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.entity.Player;

public class EffectReward extends Reward {

    @Getter private final Effect effect;
    private final PlayerDataManager playerDataManager;

    public EffectReward(BiomeMastery main,
                        PlayerDataManager playerDataManager,
                        EffectsManager effectsManager,
                        EffectType effect,
                        boolean isTemporary) {
        super(RewardType.EFFECT, isTemporary, main.getMessageManager());
        this.effect = effect.getInstance(main, effectsManager.getConfig());
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void give(Player player) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (effect.hasConflict(playerData)) {
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDCONFLICT, toString())));
            return;
        }

        effect.activate(player);
        playerData.addActiveReward(this);
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDACTIVATED, toString())));
    }

    public void remove(Player player) {
        effect.deActivate(player);
        playerDataManager.getPlayerData(player.getUniqueId()).clearActiveReward(this);
    }

    @Override
    public String toString() {
        return effect.toString() + " effect";
    }
}
