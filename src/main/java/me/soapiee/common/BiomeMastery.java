package me.soapiee.common;

import com.zaxxer.hikari.pool.HikariPool;
import lombok.Getter;
import me.soapiee.common.commands.AdminCmd;
import me.soapiee.common.commands.UsageCmd;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.hooks.PlaceHolderAPIHook;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.listeners.PlayerListener;
import me.soapiee.common.manager.CommandCooldownManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.manager.PendingRewardsManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.naming.CommunicationException;
import java.io.IOException;
import java.sql.SQLException;

public final class BiomeMastery extends JavaPlugin {

    @Getter private DataManager dataManager;
    @Getter private MessageManager messageManager;
    @Getter private PlayerCache playerCache;
    private VaultHook vaultHook;
    @Getter private Logger customLogger;
    @Getter private PlayerListener playerListener;
    @Getter private PendingRewardsManager pendingRewardsManager;
    @Getter private CommandCooldownManager cooldownManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        playerCache = new PlayerCache(Bukkit.getServer().getOfflinePlayers());
        messageManager = new MessageManager(this);
        customLogger = new Logger(this);
        cooldownManager = new CommandCooldownManager(this, getConfig().getInt("settings.command_cooldown", 3));

        // Hooks
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceHolderAPIHook(messageManager, dataManager).register();
            Utils.consoleMsg(ChatColor.GREEN + "Hooked into PlaceholderAPI");
        }
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook();
            Utils.consoleMsg(ChatColor.GREEN + "Hooked into Vault");
        } else {
            vaultHook = null;
            Utils.consoleMsg(ChatColor.RED + "Error hooking into Vault");
        }

        // Data setup
        boolean debugMode = getConfig().getBoolean("debug_mode", false);
        dataManager = new DataManager(getConfig(), messageManager, vaultHook, customLogger, cooldownManager, debugMode);

        try {
            dataManager.initialise(this);
        } catch (SQLException | CommunicationException | HikariPool.PoolInitializationException e) {
            customLogger.logToFile(e, ChatColor.RED + "Database could not connect. Switching to file storage");
            try {
                dataManager.initialiseFiles(this);
            } catch (IOException error) {
                customLogger.logToFile(e, ChatColor.RED + "There was an error creating the player data folder. Disabling plugin..");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

        } catch (IOException e) {
            customLogger.logToFile(e, ChatColor.RED + "There was an error creating the player data folder. Disabling plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        dataManager.startChecker(this);
        pendingRewardsManager = new PendingRewardsManager(this);

        // Commands setup
        getCommand("abiomemastery").setExecutor(new AdminCmd(this));
        getCommand("biomemastery").setExecutor(new UsageCmd(this));

        // Listeners setup
        playerListener = new PlayerListener(this);
        getServer().getPluginManager().registerEvents(playerListener, this);

        // Updater notification setup
        // TODO: Enable later
//        UpdateChecker updateChecker = new UpdateChecker(this, 125077);
//        updateChecker.updateAlert(this);
    }

    @Override
    public void onDisable() {
        if (pendingRewardsManager != null) pendingRewardsManager.save();
        if (cooldownManager != null) cooldownManager.save();
        if (dataManager == null) return;

        //Remove all active rewards
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());
            if (playerData.hasActiveRewards()) {
                playerData.clearActiveRewards();
            }
        }

        //Save player data
        dataManager.saveAll(false);

        if (dataManager.getDatabase() != null) dataManager.getDatabase().disconnect();

    }

    public VaultHook getVaultHook() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return null;
        else return vaultHook;
    }

    public boolean isDebugMode() {
        return getConfig().getBoolean("debug_mode", false);
    }
}
