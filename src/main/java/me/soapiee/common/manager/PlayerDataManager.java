package me.soapiee.common.manager;

import me.soapiee.common.data.PlayerData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerDataManager() {}

    public void add(PlayerData data) {
        playerDataMap.put(data.getPlayer().getUniqueId(), data);
    }

    public void remove(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public boolean has(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public void saveAll(boolean async) {
        for (UUID uuid : playerDataMap.keySet()) {
            getPlayerData(uuid).saveData(async);
        }
    }
}
