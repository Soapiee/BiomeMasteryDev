package me.soapiee.common.logic;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class LevelUpEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final OfflinePlayer player;
    private final int newLevel;
    private boolean cancelled;

    public LevelUpEvent(OfflinePlayer player, int newLevel) {
        this.player = player;
        this.newLevel = newLevel;
        this.cancelled = false;
    }

    public OfflinePlayer getOfflinePlayer() {
        return this.player;
    }

    public int getNewLevel() {
        return this.newLevel;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = true;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
