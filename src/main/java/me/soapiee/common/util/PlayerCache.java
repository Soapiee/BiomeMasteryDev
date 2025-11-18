package me.soapiee.common.util;

import lombok.Getter;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlayerCache {

    @Getter private final Set<OfflinePlayer> offlinePlayers;

    public PlayerCache(OfflinePlayer[] offlinePlayers) {
        this.offlinePlayers = new HashSet<>();
        this.offlinePlayers.addAll(Arrays.asList(offlinePlayers));
    }

    public void addOfflinePlayer(OfflinePlayer offlinePlayer) {
        offlinePlayers.add(offlinePlayer);
    }

    public OfflinePlayer getOfflinePlayer(String name) {

        for (OfflinePlayer offlinePlayer : offlinePlayers) {
            if (offlinePlayer.getName().equalsIgnoreCase(name)) {
                return offlinePlayer;
            }
        }
        return null;
    }
}
