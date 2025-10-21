package me.soapiee.common.logic.events;

import lombok.Getter;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class BiomeChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final Player player;
    @Getter private final Biome newBiome;
    @Getter private final Biome previousBiome;
    private boolean cancelled;

    public BiomeChangeEvent(Player player, Biome previousBiome, Biome newBiome) {
        this.player = player;
        this.newBiome = newBiome;
        this.previousBiome = previousBiome;
        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = true;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
