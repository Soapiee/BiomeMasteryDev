package me.soapiee.common.logic.effects;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.effects.types.FreeFoodEffect;
import me.soapiee.common.logic.effects.types.LavaSwimmerEffect;
import me.soapiee.common.logic.effects.types.SpeedSwimmerEffect;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.function.BiFunction;

public enum EffectType {

//    BOOSTEDFISHING(c -> null), //Gives fishing buffs = checks if a fishing rod is being held and apply it to that
//    WATERBREATHING(c -> null), //Doesnt lose oxygen
//    WATERFIGHTER(c -> null), //Deal double damage when in water
//    FIGHTER(c -> null), //Deal double damage to hostile mobs
    LAVASWIMMER(LavaSwimmerEffect::new), //Fire res and speed in lava
//    FOODRESTORATION((m, c) -> null), //Restores hunger when killing mobs
    FREEFOOD(FreeFoodEffect::new), //Restores hunger overtime
    SPEEDSWIMMER(SpeedSwimmerEffect::new), //Swim faster in water
//    LOOT(c -> null), //Extra loot drops
//    FALLDAMAGE(c -> null), //No fall damage
//    WOLFWHISPER(c -> null), //Spawns 5 tamed wolves
//    WATERWALKER(c -> null), //Floor turns to ice when walking on water
//    LAVAWALKER(c -> null), //Floor turns to obsidian when walking on lava
//    FIREBALL(c -> null), //Throws fireballs
//    TELEPORTER(c -> null), //Shift-right click to throw an enderpearl. Has cooldown
//    TREEFELLER(c -> null), //Cuts down a whole tree
    NONE((m, c) -> null);

    private Effect instance;
    private final BiFunction<BiomeMastery, FileConfiguration, Effect> constructor;

    EffectType(BiFunction<BiomeMastery, FileConfiguration, Effect> constructor) {
        this.constructor = constructor;
    }

    public synchronized Effect getInstance(BiomeMastery main, FileConfiguration config) {
        if (instance == null) {
            instance = constructor.apply(main, config);
        }
        return instance;
    }
}
