package me.soapiee.common.listeners;

import lombok.Setter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.effects.EffectType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class EffectsListener implements Listener {

    private final BiomeMastery main;
    private final HashMap<EffectType, HashSet<UUID>> activeEffects = new HashMap<>();

    @Setter private double lavaSwimmingSpeed;
    @Setter private double waterSwimmingSpeed;

    public EffectsListener(BiomeMastery main) {
        this.main = main;
    }

    public HashSet<UUID> getSet(EffectType type) {
        return activeEffects.get(type);
    }

    public boolean hasActiveEffect(EffectType type, UUID uuid) {
        if (!activeEffects.containsKey(type)) {
            activeEffects.put(type, new HashSet<>());
            return false;
        }

        return activeEffects.get(type).contains(uuid);
    }

    public void addActiveEffect(EffectType type, UUID uuid) {
        getSet(type).add(uuid);
    }

    public void removeActiveEffect(EffectType type, UUID uuid) {
        getSet(type).remove(uuid);
    }

    @EventHandler
    public void effectListener(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        //TODO: Test compatibility with AE
        if (hasActiveEffect(EffectType.SPEEDSWIMMER, uuid)) waterSwimmer(player);
        if (hasActiveEffect(EffectType.LAVASWIMMER, uuid)) lavaSwimmer(player, event.getTo(), event.getFrom());
    }

    private void waterSwimmer(Player player) {
        if (player.getLocation().getBlock().getType() != Material.WATER) return;

        if (player.isSwimming()) {
            player.setVelocity(player.getLocation().getDirection().multiply(waterSwimmingSpeed));
        }
    }

    private void lavaSwimmer(Player player, Location getTo, Location getFrom) {
        if (player.getLocation().getBlock().getType() != Material.LAVA) return;

        // Movement delta (player input indicator)
        Vector delta = getTo.toVector().subtract(getFrom.toVector());
        delta.setY(0);

        // If player is only drifting (VERY small movement), count as NOT pressing W
        // TUNE: 0.0025 is ~"no input"
        if (delta.lengthSquared() < 0.0025) return;

        // Look direction (horizontal only)
        Vector look = player.getLocation().getDirection().setY(0).normalize();

        // Direction player is REALLY moving
        Vector moveDir = delta.clone().normalize();

        // Dot product: how much the movement matches the looking direction
        double dot = moveDir.dot(look);

        // Require strongly forward movement to apply boost
        // TUNE: 0.80 = must be mostly forward
        // If sideways or drifting, do not boost
        if (dot < 0.80) return;

        // Apply the controlled boost
        Vector added = look.clone().multiply(lavaSwimmingSpeed);
        player.setVelocity(player.getVelocity().add(added));
    }
}
