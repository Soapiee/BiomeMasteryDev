package me.soapiee.common.logic.effects.types;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.listeners.EffectsListener;
import me.soapiee.common.logic.effects.Effect;
import me.soapiee.common.logic.effects.EffectType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SpeedSwimmerEffect implements Effect {

    @Getter private final EffectType type = EffectType.SPEEDSWIMMER;
    @Getter private final String identifier;
    @Getter private final List<EffectType> conflicts;
    private final EffectsListener listener;

    private static final double SPEED_MIN = 0.1;
    private static final double SPEED_MAX = 1;
    private static final double SPEED_DEFAULT = 0.4;
    private static final float DEFAULT_WALK_SPEED = 0.2F;

    public SpeedSwimmerEffect(BiomeMastery main, FileConfiguration config) {
        listener = main.getEffectsListener();
        String key = type.name();
        identifier = config.getString(key + ".friendly_name", key);
        conflicts = Collections.unmodifiableList(loadConflicts(config, key));

        double speed = loadSpeed(config, key);
        listener.setWaterSwimmingSpeed(speed);
    }

    private double loadSpeed(FileConfiguration config, String path) {
        double speed = config.getDouble(path + ".speed", SPEED_DEFAULT);
        if (speed < SPEED_MIN || speed > SPEED_MAX) return SPEED_DEFAULT;

        return speed;
    }

    @Override
    public void activate(Player player) {
        UUID uuid = player.getUniqueId();
        if (listener.hasActiveEffect(type, uuid)) return;

        listener.addActiveEffect(type, uuid);
    }

    @Override
    public void deActivate(Player player) {
        UUID uuid = player.getUniqueId();
        if (!listener.hasActiveEffect(type, uuid)) return;

//        player.setWalkSpeed(DEFAULT_WALK_SPEED);
        listener.removeActiveEffect(type, uuid);
    }

    @Override
    public boolean isActive(Player player) {
        return listener.hasActiveEffect(type, player.getUniqueId());
    }

    @Override
    public String toString() {
        return identifier;
    }
}
