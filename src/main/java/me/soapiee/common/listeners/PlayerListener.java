package me.soapiee.common.listeners;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.events.LevelUpEvent;
import me.soapiee.common.logic.rewards.PendingReward;
import me.soapiee.common.logic.rewards.types.Reward;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.manager.PendingRewardsManager;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
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
    private final PlayerCache playerCache;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final MessageManager messageManager;
    private final PendingRewardsManager pendingRewardsManager;
    private final Logger logger;

    private final Map<UUID, Biome> playerBiomeMap;

    public PlayerListener(BiomeMastery main) {
        this.main = main;
        playerCache = main.getPlayerCache();
        playerDataManager = main.getDataManager().getPlayerDataManager();
        configManager = main.getDataManager().getConfigManager();
        messageManager = main.getMessageManager();
        pendingRewardsManager = main.getPendingRewardsManager();
        logger = main.getCustomLogger();
        playerBiomeMap = new HashMap<>();
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

        checkPendingRewards(player);
        playerBiomeMap.put(uuid, playerBiome);

        World playerWorld = player.getWorld();
        if (!configManager.isEnabledWorld(playerWorld)) return;
        if (!configManager.isEnabledBiome(playerBiome)) return;

        setBiomeStart(playerData, playerBiome);
    }

    private void updateNotif(Player player){
//        if (configManager.isUpdateNotif()) main.getUpdateChecker().updateAlert(player);
    }

    private void checkPendingRewards(Player player) {
        pendingRewardsManager.giveAll(player);
        pendingRewardsManager.removeAll(player.getUniqueId());
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

        //Player has changed biome or world
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        String playerName = player.getName();
        if (configManager.isDebugMode())
            Utils.debugMsg(playerName, ChatColor.BLUE + "Changed biome: " + previousBiome.name() + " -> " + newBiome.name());

        if (playerData.hasActiveRewards()) {
            playerData.clearActiveRewards();
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDSDEACTIVATED, previousBiome.name())));
        }

        if (configManager.isEnabledWorld(previousWorld) && (configManager.isEnabledBiome(previousBiome))) {
            if (configManager.isDebugMode())
                Utils.debugMsg(playerName, ChatColor.BLUE + "Previous biome (" + previousBiome.name() + ") is enabled, progress saved");
            setBiomeProgress(playerData, previousBiome);
        }

        if (configManager.isEnabledWorld(newWorld) && (configManager.isEnabledBiome(newBiome))) {
            if (configManager.isDebugMode())
                Utils.debugMsg(playerName, ChatColor.BLUE + "New biome (" + newBiome.name() + ") is enabled, progress started");
            setBiomeStart(playerData, newBiome);
        }
    }

    private void setBiomeProgress(PlayerData playerData, Biome previousBiome) {
        BiomeLevel previousBiomeLevel = playerData.getBiomeLevel(previousBiome);

        if (previousBiomeLevel.isMaxLevel()) return;

        previousBiomeLevel.updateProgress();
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
        World currentWorld = player.getWorld();
        Biome currentBiome = player.getLocation().getBlock().getBiome();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        if (playerData == null) {
            logger.logToFile(new NullPointerException(), "Could not find " + player.getName() + " players data");
            return;
        }

        if (playerData.hasActiveRewards()) playerData.clearActiveRewards();

        if (configManager.isEnabledWorld(currentWorld))
            if (configManager.isEnabledBiome(currentBiome)) setBiomeProgress(playerData, currentBiome);

        playerData.saveData(true);

        playerDataManager.remove(uuid);
        playerBiomeMap.remove(uuid);
    }

    @EventHandler
    public void onLevelUp(LevelUpEvent event) {
        OfflinePlayer offlinePlayer = event.getOfflinePlayer();
        BiomeLevel biomeLevel = event.getBiomeLevel();
        Reward reward = biomeLevel.getReward(event.getNewLevel());

        if (!offlinePlayer.isOnline()) {
            if (!reward.isTemporary()) return;
            if (configManager.isDebugMode()) Utils.debugMsg(offlinePlayer.getName(), "&eAdded Pending Reward");
            pendingRewardsManager.add(offlinePlayer.getUniqueId(), new PendingReward(event.getNewLevel(), biomeLevel.getBiome(), reward));
            return;
        }

        Player player = offlinePlayer.getPlayer();
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.LEVELLEDUP, event.getNewLevel(), biomeLevel.getBiome())));

        if (!reward.isTemporary()) {
            Biome playerLocation = player.getLocation().getBlock().getBiome();
            if (!playerLocation.name().equalsIgnoreCase(biomeLevel.getBiome())) {
                player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.NOTINBIOME,biomeLevel.getBiome(), reward.toString())));
                return;
            }
        }

        reward.give(player);
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDACTIVATED, reward.toString())));
    }

}
