package me.soapiee.common.logic.effects;

import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.logic.rewards.types.EffectReward;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface Effect {

    EffectType getType();

    List<EffectType> getConflicts();

    String getIdentifier();

    default boolean hasConflict(PlayerData playerData) {
        if (getConflicts().isEmpty()) return false;

        List<EffectReward> activeEffects = playerData.getActiveRewards().stream()
                .filter(r -> r.getType() == RewardType.EFFECT)
                .map(r -> (EffectReward) r)
                .collect(Collectors.toList());
        if (activeEffects.isEmpty()) return false;

        for (EffectType type : getConflicts()) {
            for (EffectReward reward : activeEffects) {
                if (reward.getEffect().getType() == type) return true;
            }
        }
        return false;
    }

    default List<EffectType> loadConflicts(FileConfiguration config, String path) {
        List<EffectType> result = new ArrayList<>();

        if (config.isSet(path + ".effect_conflicts")){
            for (String s : config.getStringList(path + ".effect_conflicts")) {
                try {
                    result.add(EffectType.valueOf(s.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
//                main.getLogger().warning("Invalid conflict type: " + s);
                }
            }
        }

        return result;
    }

    void activate(Player player);

    void deActivate(Player player);

    boolean isActive(Player player);
}
