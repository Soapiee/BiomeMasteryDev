package me.soapiee.common.data;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.logic.ProgressChecker;
import me.soapiee.common.logic.CommandCooldown;
import me.soapiee.common.logic.rewards.EffectType;
import me.soapiee.common.logic.rewards.RewardType;
import me.soapiee.common.logic.rewards.types.*;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import javax.naming.CommunicationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class DataManager {

    private final MessageManager messageManager;
    private final Logger logger;
    private final VaultHook vaultHook;
    private FileConfiguration config;
    private boolean debugMode;

    @Getter private String dataSaveType;
    @Getter private HikariCPConnection database;

    private final HashMap<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final HashMap<Biome, BiomeData> biomeDataMap = new HashMap<>();
    @Getter private final HashSet<World> enabledWorlds = new HashSet<>();
    @Getter private final HashSet<Biome> enabledBiomes = new HashSet<>();
    @Getter private final HashMap<Integer, Integer> defaultLevels = new HashMap<>();
    @Getter private final HashMap<Integer, Reward> defaultRewards = new HashMap<>();
    @Getter private int updateInterval;
    @Getter private final CommandCooldown commandCooldown;
    private ProgressChecker progressChecker;

    public DataManager(FileConfiguration config,
                       MessageManager messageManager,
                       VaultHook vaultHook,
                       Logger logger,
                       boolean debugMode) {
        this.messageManager = messageManager;
        this.logger = logger;
        this.vaultHook = vaultHook;
        this.config = config;
        this.debugMode = debugMode;

        setDefaultSettings(Bukkit.getConsoleSender());
        createAllBiomeData(Bukkit.getConsoleSender());
        updateInterval = config.getInt("settings.update_interval", 60);
        commandCooldown = new CommandCooldown(config.getInt("settings.command_cooldown", 3));
    }

    public void initialise(BiomeMastery main) throws IOException, SQLException, CommunicationException {
        if (config.getBoolean("database.enabled")) {
            dataSaveType = "database";
            database = new HikariCPConnection(main.getConfig());
            database.connect();
        } else {
            database = null;
            dataSaveType = "files";

            Files.createDirectories(Paths.get(main.getDataFolder() + File.separator + "playerData"));

            Utils.consoleMsg(ChatColor.DARK_GREEN + "File Storage enabled.");
        }
    }

    public void reloadData(BiomeMastery main) {
        main.reloadConfig();
        debugMode = main.isDebugMode();
        config = main.getConfig();
        updateInterval = config.getInt("settings.update_interval", 60);
        commandCooldown.updateThreshold(config.getInt("settings.command_cooldown", 3));
//
        startChecker(main);
    }

    private void setDefaultSettings(CommandSender sender) {
        //Create list of enabled worlds
        enabledWorlds.clear();
        ArrayList<World> worldList = new ArrayList<>();
        boolean worldsListExists = config.isSet("default_biome_settings.enabled_worlds");
        if (worldsListExists) {
            for (String worldString : config.getStringList("default_biome_settings.enabled_worlds")) {
                World world = Bukkit.getWorld(worldString);
                if (world != null) worldList.add(world);
            }
        }
        enabledWorlds.addAll(worldList);

        //Create default levels + rewards
        defaultLevels.clear();
        defaultRewards.clear();

        ConfigurationSection levelsSection = config.getConfigurationSection("default_biome_settings.levels");
        if (levelsSection != null) {
            for (String key : config.getConfigurationSection("default_biome_settings.levels").getKeys(false)) {
                defaultLevels.put(Integer.parseInt(key), config.getInt("default_biome_settings.levels." + key + ".target_duration"));
                defaultRewards.put(Integer.parseInt(key), createReward(sender, "default_biome_settings.levels." + key + "."));
            }
        }

        //Make list of enabled biomes + create the biome data
        enabledBiomes.clear();
        boolean whiteList = config.getBoolean("default_biome_settings.use_blacklist_as_whitelist", true);
        if (!config.isSet("default_biome_settings.biomes_blacklist")){
            config.set("default_biome_settings.biomes_blacklist", new ArrayList<>());
        }
        List<String> listedBiomes = config.getStringList("default_biome_settings.biomes_blacklist");

        if (whiteList) enabledBiomes.addAll(createBiomeWhitelist(sender, listedBiomes));
        else enabledBiomes.addAll(createBiomeBlacklist(listedBiomes));
    }

    public List<Biome> createBiomeBlacklist(List<String> listedBiomes) {
        List<Biome> blacklist = new ArrayList<>();

        for (Biome biome : Biome.values()) {
            if (listedBiomes.contains(biome.name())) continue;
            blacklist.add(biome);
        }

        return blacklist;
    }

    public List<Biome> createBiomeWhitelist(CommandSender sender, List<String> listedBiomes) {
        List<Biome> whitelist = new ArrayList<>();

        for (String rawBiome : listedBiomes) {
            Biome biome;

            try {
                biome = Biome.valueOf(rawBiome.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.logToPlayer(sender, e, "&c'" + rawBiome + "' is not a valid biome");
                continue;
            }

            whitelist.add(biome);
        }

        return whitelist;
    }

    private void createAllBiomeData(CommandSender sender) {
        for (Biome enabledBiome : enabledBiomes) {
            createBiomeData(sender, enabledBiome);
        }
    }

    public void createBiomeData(CommandSender sender, Biome biome){
        if (debugMode) Utils.debugMsg("", "&eEnabled biome: " + biome.name());

        boolean isDefault = config.getConfigurationSection("biomes." + biome.name()) == null;
        if (debugMode) Utils.debugMsg("", "&eIs default: " + isDefault);

        BiomeData biomeData = new BiomeData(this, config, biome, isDefault, sender);
        biomeDataMap.put(biome, biomeData);
    }

    public Reward createReward(CommandSender sender, String path) {
        RewardType rewardType;
        try {
            rewardType = RewardType.valueOf(config.getString(path + "reward_type").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            rewardType = RewardType.NONE;
            String[] pathParts = path.split("\\.");
            logger.logToPlayer(sender, e, "&cBiome " + pathParts[1] + " at level " + pathParts[2] + " has an invalid reward type");
        }

        switch (rewardType) {
            case POTION:
                return potionReward(sender, path);

            case EFFECT:
                return effectReward(sender, path);

            case CURRENCY:
                return currencyReward(sender, path);

            case EXPERIENCE:
                return experienceReward(sender, path);

            case ITEM:
                return itemReward(sender, path);

            case PERMISSION:
                return permissionReward(sender, path);

            case COMMAND:
                return commandReward(sender, path);
        }

        return new NullReward();
    }

    private Reward potionReward(CommandSender sender, String path) {
        String[] potionParts = config.getString(path + "reward_item").split(":");
        PotionType potionType;
        int amplifier;

        try {
            amplifier = Integer.parseInt(potionParts[1]);
            potionType = PotionType.valueOf(potionParts[0].toUpperCase());
        } catch (IllegalArgumentException error) {
            if (error instanceof NumberFormatException)
                createLog(sender, path, error, "potion amplifier value");
            else
                createLog(sender, path, error, "potion type");

            return new NullReward();
        }

        String temp = config.getString(path + "type", "temporary");

        return new PotionReward(potionType, amplifier, (temp.equalsIgnoreCase("temporary")));
    }

    private Reward effectReward(CommandSender sender, String path) {
        EffectType effectType;

        try {
            effectType = EffectType.valueOf(config.getString(path + "reward_item").toUpperCase());
        } catch (IllegalArgumentException error) {
            createLog(sender, path, error, "effect type");
            return new NullReward();
        }

        boolean isTemp = config.getBoolean(path + "type", true);

        return new EffectReward(effectType, isTemp);
    }

    private Reward currencyReward(CommandSender sender, String path) {
        if (vaultHook == null) {
            createLog(sender, path, null, "vault hook");
            return new NullReward();
        }

        String rawDouble = config.getString(path + "reward_item");
        double money;

        try {
            money = Double.parseDouble(rawDouble);
        } catch (IllegalArgumentException error) {
            createLog(sender, path, error, "amount");
            return new NullReward();
        }

        if (money <= 0) {
            createLog(sender, path, null, "amount");
            return new NullReward();
        }

        return new CurrencyReward(vaultHook, money);
    }

    private Reward experienceReward(CommandSender sender, String path) {
        String rawInt = config.getString(path + "reward_item");
        int experience;

        try {
            experience = Integer.parseInt(rawInt);
        } catch (IllegalArgumentException error) {
            createLog(sender, path, error, "amount");
            return new NullReward();
        }

        if (experience <= 0) {
            createLog(sender, path, null, "amount");
            return new NullReward();
        }

        return new ExperienceReward(experience);
    }

    private Reward itemReward(CommandSender sender, String path) {
        ArrayList<ItemStack> itemList = new ArrayList<>();
        String[] itemParts;
        Material material;
        int quantity;

        if (config.isString(path + "reward_item")) {
            itemParts = config.getString(path + "reward_item").split(":");
            try {
                material = Material.valueOf(itemParts[0].toUpperCase());
                quantity = Integer.parseInt(itemParts[1].replace(":", ""));
                itemList.add(new ItemStack(material, quantity));
            } catch (IllegalArgumentException | NullPointerException error) {

                if (error instanceof NumberFormatException)
                    createLog(sender, path, error, "quantity");
                else
                    createLog(sender, path, error, "material");
            }
        }

        if (config.isList(path + "reward_item")) {
            for (String rawItemString : config.getStringList(path + "reward_item")) {
                itemParts = rawItemString.split(":");
                try {
                    material = Material.valueOf(itemParts[0].toUpperCase());
                    quantity = Integer.parseInt(itemParts[1].replace(":", ""));
                    itemList.add(new ItemStack(material, quantity));
                } catch (IllegalArgumentException | NullPointerException error) {

                    if (error instanceof NumberFormatException)
                        createLog(sender, path, error, "quantity");
                    else
                        createLog(sender, path, error, "material");
                }
            }
        }

        if (itemList.isEmpty()) return new NullReward();

        return new ItemReward(messageManager, itemList);
    }

    private Reward permissionReward(CommandSender sender, String path) {
        ArrayList<String> permissionList = new ArrayList<>();

        if (config.isString(path + "reward_item"))
            permissionList.add(config.getString(path + "reward_item"));

        if (config.isList(path + "reward_item"))
            permissionList.addAll(config.getStringList(path + "reward_item"));

        if (permissionList.isEmpty()) {
            createLog(sender, path, null, "permission");
            return new NullReward();
        }

        return new PermissionReward(vaultHook, permissionList);
    }

    private Reward commandReward(CommandSender sender, String path) {
        ArrayList<String> commandList = new ArrayList<>();

        if (config.isString(path + "reward_item"))
            commandList.add(config.getString(path + "reward_item"));

        if (config.isList(path + "reward_item"))
            commandList.addAll(config.getStringList(path + "reward_item"));

        if (commandList.isEmpty()) {
            createLog(sender, path, null, "command");
            return new NullReward();
        }

        return new CommandReward(commandList);
    }

    private void createLog(CommandSender sender, String path, Exception error, String invalidObject) {
        String[] pathParts = path.split("\\.");
        logger.logToPlayer(sender, error, "&cBiome "
                + (pathParts[1].equals("levels") ? "&edefault&c" : pathParts[1])
                + " at level " + pathParts[2] + " has an invalid " + invalidObject);
    }

    public void startChecker(BiomeMastery main) {
        if (progressChecker != null)
            try {
                progressChecker.cancel();
            } catch (IllegalStateException ignored) {
            }

        progressChecker = new ProgressChecker(main, updateInterval);
    }

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

    public boolean isEnabledWorld(World world) {
        for (World enabledWorlds : enabledWorlds) {
            if (enabledWorlds == world) return true;
        }

        return false;
    }

    public boolean isEnabledBiome(Biome biome) {
        for (Biome enabledBiome : enabledBiomes) {
            if (biome == enabledBiome) return true;
        }

        return false;
    }

    public BiomeData getBiomeData(Biome biome) {
        return biomeDataMap.getOrDefault(biome, null);
    }

    public void saveAll(boolean async) {
        for (UUID uuid : playerDataMap.keySet()) {
            getPlayerData(uuid).saveData(async);
        }
    }

}
