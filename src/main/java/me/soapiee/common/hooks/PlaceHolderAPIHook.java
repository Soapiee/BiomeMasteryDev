package me.soapiee.common.hooks;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.manager.MessageManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class PlaceHolderAPIHook extends PlaceholderExpansion {

    private final MessageManager messageManager;
    private final DataManager dataManager;

    @Override
    public @NotNull String getIdentifier() {
        return "biomemastery";
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
//            PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());

            if (identifier.equalsIgnoreCase("biomemastery_biome_level")) {
                return "example1";
            }
            if (identifier.equalsIgnoreCase("example2")) {
                return messageManager == null ? "false" : "true";
            }
        }
        return null;
    }

}
