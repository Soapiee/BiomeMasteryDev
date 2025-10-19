package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.EffectType;
import me.soapiee.common.data.rewards.RewardType;
import me.soapiee.common.data.rewards.types.*;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.logic.Checker;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DataManager {

    private final MessageManager messageManager;
    private final Logger logger;
    private final VaultHook vaultHook;
    private final FileConfiguration config;

    private String dataSaveType;
    private HikariCPConnection database;

    private final HashMap<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final HashMap<Biome, BiomeData> biomeDataMap = new HashMap<>();
    private final List<String> enabledWorlds = new ArrayList<>();
    private final List<Biome> enabledBiomes = new ArrayList<>();
    private final HashMap<Integer, Integer> defaultLevels = new HashMap<>();
    private final HashMap<Integer, Reward> defaultRewards = new HashMap<>();
    private int updateInterval;
    private Checker progressChecker;

    public DataManager(FileConfiguration config,
                       MessageManager messageManager,
                       VaultHook vaultHook,
                       Logger logger) {
        this.messageManager = messageManager;
        this.logger = logger;
        this.vaultHook = vaultHook;
        this.config = config;
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

    public void loadData(BiomeMastery main, CommandSender sender) {
        setDefaultSettings(sender);
        createBiomeData(sender);
        startChecker(main);
    }

    private void setDefaultSettings(CommandSender sender) {
        updateInterval = config.getInt("default_settings.update_interval");

        //Create list of enabled worlds
        enabledWorlds.clear();
        enabledWorlds.addAll(config.getStringList("default_settings.enabled_worlds"));

        //Create default levels + rewards
        defaultLevels.clear();
        defaultRewards.clear();

        ConfigurationSection levelsSection = config.getConfigurationSection("default_settings.levels");
        if (levelsSection != null) {
            for (String key : config.getConfigurationSection("default_settings.levels").getKeys(false)) {
                defaultLevels.put(Integer.parseInt(key), config.getInt("default_settings.levels." + key + "target_duration"));
                defaultRewards.put(Integer.parseInt(key), createReward(sender, "default_settings.levels." + key + "."));
            }
        }

        //Make list of enabled biomes + create the biome data
        enabledBiomes.clear();
        boolean whiteList = config.getBoolean("default_settings.use_blacklist_as_whitelist");
        List<String> listedBiomes = config.getStringList("default_settings.biomes_blacklist");

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

    private void createBiomeData(CommandSender sender) {
        //Loops through the list of enabled biomes. Find it in the biomes config section, if it doesnt exist then set default settings
        for (Biome enabledBiome : enabledBiomes) {
            boolean isDefault = config.getConfigurationSection("biomes." + enabledBiome.name()) == null;

            BiomeData biomeData = new BiomeData(this, config, enabledBiome, isDefault, sender);
            biomeDataMap.put(enabledBiome, biomeData);
        }
    }

    public Reward createReward(CommandSender sender, String path) {
        RewardType rewardType;
        try {
            rewardType = RewardType.valueOf(config.getString(path + "reward_type").toUpperCase());
        } catch (IllegalArgumentException ex) {
            rewardType = RewardType.NONE;
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

        logger.logToPlayer(sender, null, "&c" + rewardType + " is not a valid reward type");
        return new NullReward();
    }

    private Reward potionReward(CommandSender sender, String path) {
        String[] potionParts = config.getString(path + "reward_item").split(":");
        PotionType potionType;
        int amplifier;

        try {
            potionType = PotionType.valueOf(potionParts[0].toUpperCase());
            amplifier = Integer.parseInt(potionParts[1]);
        } catch (IllegalArgumentException ex) {
            return new NullReward();
        }

        return new PotionReward(potionType, amplifier);
    }

    private Reward effectReward(CommandSender sender, String path) {
        EffectType effectType;

        try {
            effectType = EffectType.valueOf(config.getString(path + "reward_item"));
        } catch (IllegalArgumentException ex) {
            return new NullReward();
        }

        return new EffectReward(effectType);
    }

    private Reward currencyReward(CommandSender sender, String path) {
        if (vaultHook == null) {
//            logger.logToPlayer(sender, null, ChatColor.RED + "Cannot give currency rewards as Vault could not be hooked into");
            return new NullReward();
        }

        String rawDouble = config.getString(path + "reward_item");
        double money;

        try {
            money = Double.parseDouble(rawDouble);
        } catch (IllegalArgumentException ex) {
//            logger.logToPlayer(sender, ex, "&c" + rawDouble + " is not a valid number");
            return new NullReward();
        }

        return new CurrencyReward(vaultHook, money);
    }

    private Reward experienceReward(CommandSender sender, String path) {
        String rawInt = config.getString(path + "reward_item");
        int experience;

        try {
            experience = Integer.parseInt(rawInt);
        } catch (IllegalArgumentException ex) {
            logger.logToPlayer(sender, ex, "&c" + rawInt + " is not a valid number");
            return new NullReward();
        }
        return new ExperienceReward(experience);
    }

    private Reward itemReward(CommandSender sender, String path) {
        ArrayList<ItemStack> itemList = new ArrayList<>();
        String[] itemParts;
        Material material;
        int amount;

        if (config.isString(path + "reward_item")) {
            itemParts = config.getString(path + "reward_item").split(":");
            try {
                material = Material.valueOf(itemParts[0].toUpperCase());
                amount = Integer.parseInt(itemParts[1].replace(":", ""));
                itemList.add(new ItemStack(material, amount));
            } catch (NullPointerException ex) {
                logger.logToPlayer(sender, ex, "&c" + itemParts[0] + " is not a valid material");
            } catch (NumberFormatException ex) {
                logger.logToPlayer(sender, ex, "&c" + itemParts[1] + " is not a valid number");
            }
        }

        if (config.isList(path + "reward_item")) {
            for (String rawItemString : config.getStringList(path + "reward_item")) {
                itemParts = rawItemString.split(":");
                try {
                    material = Material.valueOf(itemParts[0].toUpperCase());
                    amount = Integer.parseInt(itemParts[1].replace(":", ""));
                    itemList.add(new ItemStack(material, amount));
                } catch (NullPointerException ex) {
                    logger.logToPlayer(sender, ex, "&c" + itemParts[0] + " is not a valid material");
                } catch (NumberFormatException ex) {
                    logger.logToPlayer(sender, ex, "&c" + itemParts[1] + " is not a valid number");
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


        if (permissionList.isEmpty()) return new NullReward();

        return new PermissionReward(vaultHook, permissionList);
    }

    private Reward commandReward(CommandSender sender, String path) {
        ArrayList<String> commandList = new ArrayList<>();

        if (config.isString(path + "reward_item"))
            commandList.add(config.getString(path + "reward_item"));


        if (config.isList(path + "reward_item"))
            commandList.addAll(config.getStringList(path + "reward_item"));


        if (commandList.isEmpty()) return new NullReward();

        return new CommandReward(commandList);

    }

    public void startChecker(BiomeMastery main) {
        if (progressChecker != null)
            if (!progressChecker.isCancelled()) progressChecker.cancel();

        progressChecker = new Checker(main, updateInterval);
    }

    public void add(PlayerData data) {
        playerDataMap.put(data.uuid, data);
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

    public String getDataSaveType() {
        return dataSaveType;
    }

    public HikariCPConnection getDatabase() {
        return database;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public List<String> getEnabledWorlds() {
        return enabledWorlds;
    }

    public BiomeData getBiomeData(Biome biome) {
        return biomeDataMap.get(biome);
    }

    public HashMap<Integer, Integer> getDefaultLevels() {
        return defaultLevels;
    }

    public HashMap<Integer, Reward> getDefaultRewards() {
        return defaultRewards;
    }

    public void saveAll(boolean async) {
        for (UUID uuid : playerDataMap.keySet()) {
            getPlayerData(uuid).saveData(async);
        }
    }

}
