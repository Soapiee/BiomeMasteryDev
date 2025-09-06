package me.soapiee.common.logic;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class BiomeChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Biome newBiome;
    private final Biome previousBiome;
    private boolean cancelled;

    public BiomeChangeEvent(Player player, Biome previousBiome, Biome newBiome) {
        this.player = player;
        this.newBiome = newBiome;
        this.previousBiome = previousBiome;
        this.cancelled = false;
    }

    public Player getPlayer() {
        return player;
    }

    public Biome getNewBiome() {
        return newBiome;
    }

    public Biome getPreviousBiome() {
        return previousBiome;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = true;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
