package me.soapiee.common.logic.events;

import lombok.Getter;
import me.soapiee.common.logic.BiomeLevel;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class LevelUpEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final OfflinePlayer offlinePlayer;
    @Getter private final int newLevel;
    @Getter private final BiomeLevel biomeLevel;
    private boolean cancelled;

    public LevelUpEvent(OfflinePlayer player, int newLevel, BiomeLevel biomeLevel) {
        this.offlinePlayer = player;
        this.newLevel = newLevel;
        this.biomeLevel = biomeLevel;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = true;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
