package me.soapiee.common.logic;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.UUID;

public class CommandCooldown {
    private final HashMap<UUID, LocalDateTime> cooldowns;
    private int threshold;

    public CommandCooldown(int threshold) {
        this.cooldowns = new HashMap<>();
        this.threshold = Math.max(threshold, 1);
    }

    public void updateThreshold(int threshold) {
        this.threshold = Math.max(threshold, 1);
    }

    public void addCooldown(CommandSender sender) {
        UUID uuid = getUUID(sender);
        cooldowns.put(uuid, LocalDateTime.now());
    }

    private UUID getUUID(CommandSender sender){
        UUID uuid;
        if (sender instanceof Player) uuid = ((Player) sender).getUniqueId();
        else uuid = UUID.fromString("2fcaf22d-9f2d-41f3-bb31-ff220e85c685");

        return uuid;
    }

    public long getCooldown(CommandSender sender) {
        UUID uuid = getUUID(sender);
        if (!cooldowns.containsKey(uuid)) return 0;

        long difference = ChronoUnit.SECONDS.between(cooldowns.get(uuid), LocalDateTime.now());
        long timeRemaining = threshold - difference;

        if (timeRemaining <= 0) cooldowns.remove(uuid);

        return (timeRemaining < 0 ? 0 : timeRemaining);
    }
}
