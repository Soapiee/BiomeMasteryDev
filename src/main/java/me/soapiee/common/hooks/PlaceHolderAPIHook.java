package me.soapiee.common.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.soapiee.common.BiomeMastery;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceHolderAPIHook extends PlaceholderExpansion {

    private final BiomeMastery main;

    public PlaceHolderAPIHook(BiomeMastery main) {
        this.main = main;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tfquiz";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Soapiee";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        if (offlinePlayer != null && offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();

            if (identifier.equalsIgnoreCase("example1")) {
                return "example1";
            }
            if (identifier.equalsIgnoreCase("example2")) {
                return main.getMessageManager() == null ? "false" : "true";
            }
        }
        return null;
    }

}
