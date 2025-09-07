package me.soapiee.common;

import me.soapiee.common.commands.AdminCmd;
import me.soapiee.common.commands.UsageCmd;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.hooks.PlaceHolderAPIHook;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.listeners.PlayerListener;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import javax.naming.CommunicationException;
import java.io.IOException;
import java.sql.SQLException;

public final class BiomeMastery extends JavaPlugin {

    private DataManager dataManager;
    private MessageManager messageManager;
    private PlayerCache playerCache;
    private VaultHook vaultHook;
    private Logger logger;


    //TODO: Save level progress on BiomeChangeEvent
    //TODO: Runnable > Every 1 minute and +1 progress to each online player
    //TODO: Runnable > Every X time, check progress


    @Override
    public void onEnable() {
        saveDefaultConfig();

        playerCache = new PlayerCache();
        messageManager = new MessageManager(this);
        logger = new Logger(this);

        try {
            dataManager = new DataManager(this);
        } catch (SQLException | CommunicationException e) {
            logger.logToFile(e, ChatColor.RED + "Database could not connect. Disabling plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } catch (IOException e) {
            logger.logToFile(e, ChatColor.RED + "There was an error creating the player data folder. Disabling plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) new PlaceHolderAPIHook(this).register();
        if (getServer().getPluginManager().getPlugin("Vault") != null) vaultHook = new VaultHook();

        getCommand("admin").setExecutor(new AdminCmd(this));
        getCommand("placeholder").setExecutor(new UsageCmd(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // TODO: Enable later
//        UpdateChecker updateChecker = new UpdateChecker(this, 125077);
//        updateChecker.updateAlert(this);
    }

    @Override
    public void onDisable() {
        if (dataManager == null) return;

        dataManager.saveAll(false);

        if (dataManager.getDatabase() != null) {
            dataManager.getDatabase().disconnect();
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PlayerCache getPlayerCache() {
        return playerCache;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public Logger getCustomLogger() {
        return logger;
    }
}
