package me.soapiee.common.listeners;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.events.LevelUpEvent;
import me.soapiee.common.logic.rewards.PendingReward;
import me.soapiee.common.logic.rewards.types.EffectReward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.logic.rewards.types.Reward;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.manager.PendingRewardsManager;
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
    private final PendingRewardsManager pendingRewardsManager;
    private final Logger logger;

    private final Map<UUID, Biome> playerBiomeMap;

    public PlayerListener(BiomeMastery main) {
        this.main = main;
        dataManager = main.getDataManager();
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

        PlayerData playerData;
        if (!dataManager.has(player.getUniqueId())) {
            try {
                playerData = new PlayerData(main, player);
                dataManager.add(playerData);
            } catch (IOException | SQLException error) {
                logger.logToPlayer(player, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                return;
            }
        } else playerData = dataManager.getPlayerData(player.getUniqueId());

        checkPendingRewards(player);
        playerBiomeMap.put(uuid, playerBiome);

        World playerWorld = player.getWorld();
        if (!dataManager.isEnabledWorld(playerWorld)) return;
        if (!dataManager.isEnabledBiome(playerBiome)) return;

        setBiomeStart(playerData, playerBiome);
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
        PlayerData playerData = dataManager.getPlayerData(uuid);
        String playerName = player.getName();
        if (main.isDebugMode())
            Utils.debugMsg(playerName, ChatColor.BLUE + "Changed biome: " + previousBiome.name() + " -> " + newBiome.name());

        if (playerData.hasActiveRewards()) {
            playerData.clearActiveRewards();
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDSDEACTIVATED, previousBiome.name())));
        }

        if (dataManager.isEnabledWorld(previousWorld) && (dataManager.isEnabledBiome(previousBiome))) {
            if (main.isDebugMode())
                Utils.debugMsg(playerName, ChatColor.BLUE + "Previous biome (" + previousBiome.name() + ") is enabled, progress saved");
            setBiomeProgress(playerData, previousBiome);
        }

        if (dataManager.isEnabledWorld(newWorld) && (dataManager.isEnabledBiome(newBiome))) {
            if (main.isDebugMode())
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
        PlayerData playerData = dataManager.getPlayerData(uuid);

        if (playerData == null) {
            logger.logToFile(new NullPointerException(), "Could not find " + player.getName() + " players data");
            return;
        }

        if (playerData.hasActiveRewards()) playerData.clearActiveRewards();

        if (dataManager.isEnabledWorld(currentWorld)) {
            if (dataManager.isEnabledBiome(currentBiome)) setBiomeProgress(playerData, currentBiome);
        }

        playerData.saveData(true);

        dataManager.remove(uuid);
        playerBiomeMap.remove(uuid);
    }

    @EventHandler
    public void onLevelUp(LevelUpEvent event) {
        OfflinePlayer offlinePlayer = event.getOfflinePlayer();
        BiomeLevel biomeLevel = event.getBiomeLevel();
        Reward reward = biomeLevel.getReward(event.getNewLevel());
        PlayerData playerData = dataManager.getPlayerData(offlinePlayer.getUniqueId());

        if (!offlinePlayer.isOnline()) {
            if (!reward.isTemporary()) return;
            if (main.isDebugMode()) Utils.debugMsg(offlinePlayer.getName(), "&eAdded Pending Reward");
            pendingRewardsManager.add(offlinePlayer.getUniqueId(), new PendingReward(event.getNewLevel(), biomeLevel.getBiome(), reward));
            return;
        }

        if (reward instanceof PotionReward || reward instanceof EffectReward) playerData.addActiveReward(reward);

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
