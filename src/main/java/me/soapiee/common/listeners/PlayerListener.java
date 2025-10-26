package me.soapiee.common.listeners;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.BiomeData;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.events.LevelUpEvent;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final MessageManager messageManager;
    private final Logger logger;

    private final Map<UUID, Biome> playerBiomeMap;

    public PlayerListener(BiomeMastery main) {
        this.main = main;
        dataManager = main.getDataManager();
        messageManager = main.getMessageManager();
        logger = main.getCustomLogger();
        playerBiomeMap = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Biome playerBiome = player.getLocation().getBlock().getBiome();

        if (!dataManager.has(player.getUniqueId())) {
            try {
                PlayerData playerData = new PlayerData(main, player);
                dataManager.add(playerData);
            } catch (IOException | SQLException error) {
                logger.logToPlayer(player, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
            }
        }

        playerBiomeMap.put(uuid, playerBiome);

        World playerWorld = player.getWorld();
        if (!dataManager.isEnabledWorld(playerWorld)) return;
        if (!dataManager.isEnabledBiome(playerBiome)) return;

        setBiomeStart(dataManager.getPlayerData(uuid), playerBiome);
    }

    @EventHandler
    public void onBiomeChange(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Biome newBiome = event.getTo().getBlock().getBiome();

        if (!playerBiomeMap.containsKey(uuid)) playerBiomeMap.put(uuid, newBiome);

        World previousWorld = event.getFrom().getWorld();
        Biome previousBiome = event.getFrom().getBlock().getBiome();
        World newWorld = event.getTo().getWorld();
        if (previousWorld == newWorld) {
            if (previousBiome == newBiome) return;
            else if (previousBiome.name().equalsIgnoreCase(newBiome.name())) return;
        }

        PlayerData playerData = dataManager.getPlayerData(uuid);
        if (!dataManager.isEnabledWorld(previousWorld)) return;
        setBiomeProgress(playerData, previousBiome);

        if (!dataManager.isEnabledWorld(newWorld)) return;
        setBiomeStart(playerData, newBiome);
    }

    private void setBiomeProgress(PlayerData playerData, Biome previousBiome) {
        if (!dataManager.isEnabledBiome(previousBiome)) return;

        BiomeData biomeData = dataManager.getBiomeData(previousBiome);
        BiomeLevel previousBiomeLevel = playerData.getBiomeLevel(previousBiome);

        if (previousBiomeLevel.getLevel() >= biomeData.getMaxLevel()) return;

        previousBiomeLevel.addProgress();
        previousBiomeLevel.clearEntryTime();
    }

    private void setBiomeStart(PlayerData playerData, Biome newBiome) {
        if (!dataManager.isEnabledBiome(newBiome)) return;

        if (main.isDebugMode()) Utils.debugMsg(playerData.getPlayer().getName(),ChatColor.DARK_PURPLE + "new Biome: " + (newBiome != null ? newBiome.name() : "null"));
        if (newBiome == null) return;

        BiomeLevel playerLevel = playerData.getBiomeLevel(newBiome);
        playerLevel.setEntryTime(LocalDateTime.now());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        World currentWorld = player.getWorld();
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        PlayerData playerData = dataManager.getPlayerData(uuid);

        if (dataManager.isEnabledWorld(currentWorld)) {
            setBiomeProgress(playerData, currentBiome);
        }

        playerData.saveData(true);

        dataManager.remove(uuid);
        playerBiomeMap.remove(uuid);
    }

    @EventHandler
    public void onLevelUp(LevelUpEvent event) {
        //TODO: Give player their reward

        OfflinePlayer player = event.getOfflinePlayer();
        if (!player.isOnline()) return;

        ((Player) player).sendMessage(Utils.colour(ChatColor.GREEN + "You leveled up to level " + event.getNewLevel()));
    }

}
