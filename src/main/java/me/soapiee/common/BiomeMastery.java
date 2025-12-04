package me.soapiee.common;

import lombok.Getter;
import me.soapiee.common.commands.AdminCmd;
import me.soapiee.common.commands.UsageCmd;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.hooks.PlaceHolderAPIHook;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.listeners.EffectsListener;
import me.soapiee.common.listeners.LevelUpListener;
import me.soapiee.common.listeners.PlayerListener;
import me.soapiee.common.listeners.PotionRemovalListener;
import me.soapiee.common.manager.DataManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.io.IOException;

public class BiomeMastery extends JavaPlugin {

    @Getter private DataManager dataManager;
    @Getter private MessageManager messageManager;
    @Getter private PlayerCache playerCache;
    private VaultHook vaultHook;
    @Getter private Logger customLogger;
    @Getter private EffectsListener effectsListener;

    public BiomeMastery() {
        super();
    }

    //Used for MockedBukkit
    public BiomeMastery(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        playerCache = new PlayerCache(Bukkit.getServer().getOfflinePlayers());
        customLogger = new Logger(this);
        messageManager = new MessageManager(this);

        // Data setup
        dataManager = new DataManager(this);

        try {
            dataManager.initialise(this);
        } catch (IOException e) {
            customLogger.logToFile(e, ChatColor.RED + "There was an error creating/retrieving player data. Disabling plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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

        effectsListener = new EffectsListener(this);
        getServer().getPluginManager().registerEvents(effectsListener, this);

        dataManager.initialiseRewards(this);
        dataManager.initialiseBiomeData(getConfig());
        dataManager.startChecker(this);

        // Listeners setup
        PlayerListener playerListener = new PlayerListener(this, dataManager);
        getServer().getPluginManager().registerEvents(playerListener, this);
        PotionRemovalListener potionRemovalListener = new PotionRemovalListener(dataManager.getPlayerDataManager());
        getServer().getPluginManager().registerEvents(potionRemovalListener, this);
        LevelUpListener levelUpListener = new LevelUpListener(messageManager, customLogger, dataManager);
        getServer().getPluginManager().registerEvents(levelUpListener, this);

        // Commands setup
        getCommand("abiomemastery").setExecutor(new AdminCmd(this));
        getCommand("biomemastery").setExecutor(new UsageCmd(this));

        // Updater notification setup
        // TODO: Enable later
        // Updater notification setup
//        updateChecker = new UpdateChecker(this, 125077);
//        updateChecker.updateAlert(Bukkit.getConsoleSender());
    }

    @Override
    public void onDisable() {
        if (dataManager == null) return;

        //Remove all active rewards
        PlayerDataManager playerDataManager = dataManager.getPlayerDataManager();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
            if (playerData.hasActiveRewards()) {
                playerData.clearActiveRewards();
            }
        }

        dataManager.saveAll();
        if (dataManager.getDatabase() != null) dataManager.getDatabase().disconnect();
    }

    public VaultHook getVaultHook() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return null;
        else return vaultHook;
    }
}
