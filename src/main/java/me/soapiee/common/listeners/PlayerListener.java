package me.soapiee.common.listeners;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeChangeEvent;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.LevelUpEvent;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final MessageManager messageManager;
    private final Logger logger;

    private final Map<UUID, Biome> playerBiome;

    public PlayerListener(BiomeMastery main) {
        this.main = main;
        dataManager = main.getDataManager();
        messageManager = main.getMessageManager();
        logger = main.getCustomLogger();
        playerBiome = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Biome biome = player.getLocation().getBlock().getBiome();
        if (!dataManager.has(player.getUniqueId())) {
            try {
                PlayerData playerData = new PlayerData(main, player);
                dataManager.add(playerData);
                setBiomeStart(playerData, biome);
//                playerData.getBiomeData(biome).setEntryTime(LocalDateTime.now());
                playerBiome.put(player.getUniqueId(), player.getLocation().getBlock().getBiome());
                Utils.consoleMsg(ChatColor.GREEN + "Added player to playerBiome map");
            } catch (IOException | SQLException e) {
                logger.logToPlayer(player, e, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerData playerData = dataManager.getPlayerData(uuid);

        setBiomeProgress(playerData, playerBiome.get(uuid));
        playerData.saveData(true);

        dataManager.remove(player.getUniqueId());
        playerBiome.remove(player.getUniqueId());
        Utils.consoleMsg(ChatColor.RED + "Removed player from playerBiome map");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        //Cancel if player didnt move blocks

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Biome newBiome = player.getLocation().getBlock().getBiome();

        if (!playerBiome.containsKey(uuid)) {
            playerBiome.put(uuid, newBiome);
            setBiomeStart(dataManager.getPlayerData(uuid), newBiome);
//            Utils.consoleMsg(ChatColor.YELLOW + "Player wasnt in playerBiome map, added them");
            return;
        }

        Biome previousBiome = playerBiome.get(uuid);
        if (previousBiome == newBiome) return;

//        Utils.consoleMsg(ChatColor.YELLOW + "Calling BiomeChangeEvent");
        BiomeChangeEvent biomeChangeEvent = new BiomeChangeEvent(player, previousBiome, newBiome);
        Bukkit.getPluginManager().callEvent(biomeChangeEvent);
        playerBiome.put(uuid, newBiome);
    }

    @EventHandler
    public void onBiomeChange(BiomeChangeEvent event) {
        PlayerData playerData = dataManager.getPlayerData(event.getPlayer().getUniqueId());

        setBiomeProgress(playerData, event.getPreviousBiome());
        setBiomeStart(playerData, event.getNewBiome());
    }

    private void setBiomeProgress(PlayerData playerData, Biome previousBiome) {
        BiomeLevel previousBiomeData = playerData.getBiomeData(previousBiome);
        int progress = (int) ChronoUnit.SECONDS.between(previousBiomeData.getEntryTime(), LocalDateTime.now());
//        Utils.consoleMsg(ChatColor.YELLOW + "Progress: " + progress);

        //Progress is not added if they are in the biome for less than 5 seconds
        if (progress <= 5) return;

        previousBiomeData.addProgress(progress);
    }

    private void setBiomeStart(PlayerData playerData, Biome newBiome) {
//        Utils.consoleMsg(ChatColor.YELLOW + "new Biome: " + (newBiome != null ? newBiome.name() : "null"));
        if (newBiome == null) return;

        BiomeLevel newBiomeData = playerData.getBiomeData(newBiome);
        newBiomeData.setEntryTime(LocalDateTime.now());
    }

    @EventHandler
    public void onLevelUp(LevelUpEvent event) {
        OfflinePlayer player = event.getOfflinePlayer();
        if (!player.isOnline()) return;

        ((Player) player).sendMessage(Utils.colour(ChatColor.GREEN + "You leveled up to level " + event.getNewLevel()));
    }

}
