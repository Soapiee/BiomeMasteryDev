package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.EffectType;
import me.soapiee.common.data.rewards.RewardType;
import me.soapiee.common.data.rewards.types.*;
import me.soapiee.common.util.Logger;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;

public class BiomeData {

    private final BiomeMastery main;
    private final Biome biome;
    private final Logger logger;
    private final HashMap<Integer, Integer> levels;
    private final HashMap<Integer, Reward> rewards;

    public BiomeData(BiomeMastery main, Biome biome, boolean isDefault, CommandSender sender) {
        this.main = main;
        this.biome = biome;
        this.logger = main.getCustomLogger();
        levels = new HashMap<>();
        rewards = new HashMap<>();


        if (isDefault) {
            DataManager dataManager = main.getDataManager();

            levels.putAll(dataManager.getDefaultLevels());
            rewards.putAll(dataManager.getDefaultRewards());

        } else {
            FileConfiguration config = main.getConfig();
            String biomeName = biome.name();

            for (String key : config.getConfigurationSection("Biomes." + biomeName).getKeys(false)) {
                int level = Integer.parseInt(key);
                levels.put(level, config.getInt("Biomes." + biomeName + "." + level + ".target_duration"));
                rewards.put(level, createReward(sender, "Biomes." + biomeName + "." + level + "."));
            }
        }

    }

    //TODO: Move to DataManager
    private Reward createReward(CommandSender sender, String path) {
        FileConfiguration config = main.getConfig();

        RewardType rewardType;
        try {
            rewardType = RewardType.valueOf(config.getString(path));
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
        return new NullReward(main);
    }

    private Reward potionReward(CommandSender sender, String path) {
        String[] potionParts = main.getConfig().getString(path + "reward_item").split(":");
        PotionEffectType potionType;
        int amplifier;

        try {
            potionType = PotionEffectType.getByName(potionParts[0]);
            amplifier = Integer.parseInt(potionParts[1]);
        } catch (IllegalArgumentException ex) {
            return new NullReward(main);
        }

        return new PotionReward(main, potionType, amplifier);
    }

    private Reward effectReward(CommandSender sender, String path) {
        EffectType effectType;

        try {
            effectType = EffectType.valueOf(main.getConfig().getString(path + "reward_item"));
        } catch (IllegalArgumentException ex) {
            return new NullReward(main);
        }

        return new EffectReward(main, effectType);
    }

    private Reward currencyReward(CommandSender sender, String path) {
        if (main.getVaultHook() == null) {
//            logger.logToPlayer(sender, null, ChatColor.RED + "Cannot give currency rewards as Vault could not be hooked into");
            return new NullReward(main);
        }

        String rawDouble = main.getConfig().getString(path + "reward_item");
        double money;

        try {
            money = Double.parseDouble(rawDouble);
        } catch (IllegalArgumentException ex) {
//            logger.logToPlayer(sender, ex, "&c" + rawDouble + " is not a valid number");
            return new NullReward(main);
        }

        return new CurrencyReward(main, money);
    }

    private Reward experienceReward(CommandSender sender, String path) {
        String rawInt = main.getConfig().getString(path + "reward_item");
        int experience;

        try {
            experience = Integer.parseInt(rawInt);
        } catch (IllegalArgumentException ex) {
            logger.logToPlayer(sender, ex, "&c" + rawInt + " is not a valid number");
            return new NullReward(main);
        }
        return new ExperienceReward(main, experience);
    }

    private Reward itemReward(CommandSender sender, String path) {
        FileConfiguration config = main.getConfig();
        ArrayList<ItemStack> itemList = new ArrayList<>();
        String[] itemParts;
        Material material;
        int amount;

        if (config.isString(config.getString(path + "reward_item"))) {
            itemParts = config.getString(path + "reward_item").split(":");
            try {
                material = Material.valueOf(itemParts[0]);
                amount = Integer.parseInt(itemParts[1]);
                itemList.add(new ItemStack(material, amount));
            } catch (NullPointerException ex) {
                logger.logToPlayer(sender, ex, "&c" + itemParts[0] + " is not a valid material");
//                return new NullReward(main);
            } catch (NumberFormatException ex) {
                logger.logToPlayer(sender, ex, "&c" + itemParts[1] + " is not a valid number");
//                return new NullReward(main);
            }
        }

        if (config.isList(config.getString(path + "reward_item"))) {
            for (String rawItemString : config.getStringList(path + "reward_item")) {
                itemParts = rawItemString.split(":");
                try {
                    material = Material.valueOf(itemParts[0]);
                    amount = Integer.parseInt(itemParts[1]);
                    itemList.add(new ItemStack(material, amount));
                } catch (NullPointerException ex) {
                    logger.logToPlayer(sender, ex, "&c" + itemParts[0] + " is not a valid material");
                } catch (NumberFormatException ex) {
                    logger.logToPlayer(sender, ex, "&c" + itemParts[1] + " is not a valid number");
                }

            }
        }

        if (itemList.isEmpty()) return new NullReward(main);

        return new ItemReward(main, itemList);
    }

    private Reward permissionReward(CommandSender sender, String path) {
        FileConfiguration config = main.getConfig();
        ArrayList<String> permissionList = new ArrayList<>();

        if (config.isString(config.getString(path + "reward_item")))
            permissionList.add(config.getString(path + "reward_item"));


        if (config.isList(config.getString(path + "reward_item")))
            permissionList.addAll(config.getStringList(path + "reward_item"));


        if (permissionList.isEmpty()) return new NullReward(main);

        return new PermissionReward(main, permissionList);
    }

    private Reward commandReward(CommandSender sender, String path) {
        FileConfiguration config = main.getConfig();
        ArrayList<String> commandList = new ArrayList<>();

        if (config.isString(config.getString(path + "reward_item")))
            commandList.add(config.getString(path + "reward_item"));


        if (config.isList(config.getString(path + "reward_item")))
            commandList.addAll(config.getStringList(path + "reward_item"));


        if (commandList.isEmpty()) return new NullReward(main);

        return new CommandReward(main, commandList);

    }

    public int getTargetDuration(int level) {
        return levels.get(level);
    }

    public Reward getReward(int level) {
        return rewards.get(level);
    }

    public Biome getBiome() {
        return biome;
    }
}
