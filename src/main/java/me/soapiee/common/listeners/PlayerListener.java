package me.soapiee.common.listeners;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.SingularData;
import me.soapiee.common.manager.*;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final BiomeMastery main;
    private final PlayerCache playerCache;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final MessageManager messageManager;
    private final PendingRewardsManager pendingRewardsManager;
    private final Logger logger;

    private final Map<UUID, Location> prevLocMap = new HashMap<>();

    public PlayerListener(BiomeMastery main, DataManager dataManager) {
        this.main = main;
        playerCache = main.getPlayerCache();
        playerDataManager = dataManager.getPlayerDataManager();
        configManager = dataManager.getConfigManager();
        biomeDataManager = dataManager.getBiomeDataManager();
        messageManager = main.getMessageManager();
        pendingRewardsManager = dataManager.getPendingRewardsManager();
        logger = main.getCustomLogger();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Biome playerBiome = player.getLocation().getBlock().getBiome();

        if (!player.hasPlayedBefore()) playerCache.addOfflinePlayer(player);

        //TODO:
        // if (player.hasPermission("biomemastery.admin")) updateNotif(player);

        PlayerData playerData;
        if (!playerDataManager.has(player.getUniqueId())) {
            try {
                playerData = new PlayerData(main, player);
                playerDataManager.add(playerData);
            } catch (IOException | SQLException error) {
                logger.logToPlayer(player, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                return;
            }
        } else playerData = playerDataManager.getPlayerData(player.getUniqueId());

        if (checkPendingRewards(player)) givePendingRewards(player);
        prevLocMap.put(uuid, player.getLocation());

        World playerWorld = player.getWorld();
        if (!configManager.isEnabledWorld(playerWorld)) return;
        if (!configManager.isEnabledBiome(playerBiome)) return;

        setBiomeStart(playerData, playerBiome);
    }

    private void updateNotif(Player player) {
//        if (configManager.isUpdateNotif()) main.getUpdateChecker().updateAlert(player);
    }

    private boolean checkPendingRewards(Player player) {
        return pendingRewardsManager.has(player.getUniqueId());
    }

    private void givePendingRewards(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                pendingRewardsManager.giveAll(player);
                pendingRewardsManager.removeAll(player.getUniqueId());
            }
        }.runTaskLater(main, 20);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        prevLocMap.put(player.getUniqueId(), player.getLocation());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        processBiomeChange(event.getPlayer(), event.getRespawnLocation());
    }

    public boolean worldHasChanged(World previousWorld, World newWorld) {
        return previousWorld != newWorld;
    }
    public boolean hasLocationChanged(World prevWorld, World newWorld, Biome prevBiome, Biome newBiome) {
        if (worldHasChanged(prevWorld, newWorld)) return true;
        return (biomeHasChanged(prevBiome, newBiome));
    }

    private void clearActiveRewards(Player player, Biome previousBiome) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        playerData.clearActiveRewards();

        String biomeString = biomeDataManager.getBiomeData(previousBiome).getBiomeName();
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDSDEACTIVATED, biomeString)));
    }

    public boolean isLocEnabled(World world, Biome biome) {
        return (configManager.isEnabledWorld(world) && (configManager.isEnabledBiome(biome)));
    }

    private void processBiomeChange(Player player, Location newLoc) {
        UUID uuid = player.getUniqueId();
        Location prevLoc = prevLocMap.get(uuid);
        World previousWorld = prevLoc.getWorld();
        Biome previousBiome = prevLoc.getBlock().getBiome();
        World newWorld = newLoc.getWorld();
        Biome newBiome = newLoc.getBlock().getBiome();

        if (!hasLocationChanged(previousWorld, newWorld, previousBiome, newBiome)) return;
        prevLocMap.put(uuid, newLoc);

        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        if (playerData.hasActiveRewards()) clearActiveRewards(player, previousBiome);
        if (isLocEnabled(previousWorld, previousBiome)) setBiomeProgress(playerData, previousBiome);
        if (isLocEnabled(newWorld, newBiome)) setBiomeStart(playerData, newBiome);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getX() != to.getX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            UUID uuid = player.getUniqueId();
            Location newLoc = event.getTo();
            if (!prevLocMap.containsKey(uuid)) prevLocMap.put(uuid, newLoc);
            processBiomeChange(player, newLoc);
        }
    }

    private void setBiomeProgress(PlayerData playerData, Biome previousBiome) {
        BiomeLevel previousBiomeLevel = playerData.getBiomeLevel(previousBiome);

        if (previousBiomeLevel.isMaxLevel()) return;

        Biome previousBiomeParent = biomeDataManager.getBiomeData(previousBiome).getBiome();
        previousBiomeLevel.updateProgress(previousBiomeParent);
        previousBiomeLevel.clearEntryTime();
    }

    private void setBiomeStart(PlayerData playerData, Biome newBiome) {
        if (newBiome == null) return;

        BiomeLevel playerLevel = playerData.getBiomeLevel(newBiome);
        playerLevel.setEntryTime(LocalDateTime.now());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        if (playerData == null) {
            logger.logToFile(new NullPointerException(), "Could not find " + player.getName() + " players data");
            return;
        }

        if (playerData.hasActiveRewards()) playerData.clearActiveRewards();

        World currentWorld = player.getWorld();
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        if (configManager.isEnabledWorld(currentWorld))
            if (configManager.isEnabledBiome(currentBiome)) setBiomeProgress(playerData, currentBiome);

        playerData.saveData(true);

        playerDataManager.remove(uuid);
        prevLocMap.remove(uuid);
    }


//    =-=-=-=-=-=-=-=-=-=-=-=-= BIOME DATA POST GROUP UPDATE =-=-=-=-=-=-=-=-=-=-=-=-=


    public boolean biomeHasChanged(Biome previousBiome, Biome newBiome) {
        if (previousBiome == newBiome) return false;

        //check if previousBiome biomeData is singular
        BiomeData biomeData = biomeDataManager.getBiomeData(previousBiome);

        if (biomeData == null || biomeData instanceof SingularData) {
            return !previousBiome.name().equalsIgnoreCase(newBiome.name());
        }

        ArrayList<Biome> groupList = configManager.getChildren(previousBiome);
        groupList.add(biomeData.getBiome());

        return !groupList.contains(newBiome);

    }
}
