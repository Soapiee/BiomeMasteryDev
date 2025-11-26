package me.soapiee.common.logic.rewards;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.logic.effects.EffectType;
import me.soapiee.common.logic.rewards.types.*;
import me.soapiee.common.manager.EffectsManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;

public class RewardFactory {

    private final BiomeMastery main;
    private final FileConfiguration config;
    private final Logger customLogger;
    private final VaultHook vaultHook;
    private final MessageManager messageManager;
    private final PlayerDataManager playerDataManager;
    private final EffectsManager effectsManager;

    public RewardFactory(BiomeMastery main, PlayerDataManager playerDataManager, EffectsManager effectsManager) {
        this.main = main;
        this.config = main.getConfig();
        this.customLogger = main.getCustomLogger();
        this.vaultHook = main.getVaultHook();
        this.messageManager = main.getMessageManager();
        this.playerDataManager = playerDataManager;
        this.effectsManager = effectsManager;
    }

    public Reward create(String path) {
        RewardType rewardType;
        try {
            rewardType = RewardType.valueOf(config.getString(path + "reward_type").toUpperCase());
        } catch (NullPointerException | IllegalArgumentException e) {
            rewardType = RewardType.NONE;
            String[] pathParts = path.split("\\.");
            customLogger.logToFile(e, "&cBiome " + pathParts[1] + " at level " + pathParts[2] + " has an invalid reward type");
        }

        switch (rewardType) {
            case POTION:
                return potionReward(path);

            case EFFECT:
                return effectReward(path);

            case CURRENCY:
                return currencyReward(path);

            case EXPERIENCE:
                return experienceReward(path);

            case ITEM:
                return itemReward(path);

            case PERMISSION:
                return permissionReward(path);

            case COMMAND:
                return commandReward(path);
        }

        return new NullReward();
    }

    private Reward potionReward(String path) {
        String[] potionParts = config.getString(path + "reward_item").split(":");
        PotionType potionType;
        int amplifier;

        try {
            amplifier = Integer.parseInt(potionParts[1]);
            potionType = PotionType.valueOf(potionParts[0].toUpperCase());
        } catch (IllegalArgumentException error) {
            if (error instanceof NumberFormatException)
                createLog(path, error, "potion amplifier value");
            else
                createLog(path, error, "potion type");

            return new NullReward();
        }

        String temp = config.getString(path + "type", "temporary");

        return new PotionReward(main, playerDataManager, potionType, amplifier, (temp.equalsIgnoreCase("temporary")));
    }

    private Reward effectReward(String path) {
        EffectType effectType;

        try {
            effectType = EffectType.valueOf(config.getString(path + "reward_item").toUpperCase());
        } catch (IllegalArgumentException error) {
            createLog(path, error, "effect type");
            return new NullReward();
        }

        String temp = config.getString(path + "type", "temporary");

        return new EffectReward(main, playerDataManager, effectsManager, effectType, (temp.equalsIgnoreCase("temporary")));
    }

    private Reward currencyReward(String path) {
        if (vaultHook == null) {
            createLog(path, null, "vault hook");
            return new NullReward();
        }

        String rawDouble = config.getString(path + "reward_item");
        double money;

        try {
            money = Double.parseDouble(rawDouble);
        } catch (IllegalArgumentException error) {
            createLog(path, error, "amount");
            return new NullReward();
        }

        if (money <= 0) {
            createLog(path, null, "amount");
            return new NullReward();
        }

        return new CurrencyReward(main, money);
    }

    private Reward experienceReward(String path) {
        String rawInt = config.getString(path + "reward_item");
        int experience;

        try {
            experience = Integer.parseInt(rawInt);
        } catch (IllegalArgumentException error) {
            createLog(path, error, "amount");
            return new NullReward();
        }

        if (experience <= 0) {
            createLog(path, null, "amount");
            return new NullReward();
        }

        return new ExperienceReward(main, experience);
    }

    private Reward itemReward(String path) {
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
                    createLog(path, error, "quantity");
                else
                    createLog(path, error, "material");
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
                        createLog(path, error, "quantity");
                    else
                        createLog(path, error, "material");
                }
            }
        }

        if (itemList.isEmpty()) return new NullReward();

        return new ItemReward(messageManager, itemList);
    }

    private Reward permissionReward(String path) {
        ArrayList<String> permissionList = new ArrayList<>();

        if (vaultHook == null) {
            createLog(path, null, "vault hook");
            return new NullReward();
        }

        if (config.isString(path + "reward_item"))
            permissionList.add(config.getString(path + "reward_item"));

        if (config.isList(path + "reward_item"))
            permissionList.addAll(config.getStringList(path + "reward_item"));

        if (permissionList.isEmpty()) {
            createLog(path, null, "permission");
            return new NullReward();
        }

        return new PermissionReward(main, permissionList);
    }

    private Reward commandReward(String path) {
        ArrayList<String> commandList = new ArrayList<>();

        if (config.isString(path + "reward_item"))
            commandList.add(config.getString(path + "reward_item"));

        if (config.isList(path + "reward_item"))
            commandList.addAll(config.getStringList(path + "reward_item"));

        if (commandList.isEmpty()) {
            createLog(path, null, "command");
            return new NullReward();
        }

        return new CommandReward(main, commandList);
    }

    private void createLog(String path, Exception error, String invalidObject) {
        String[] pathParts = path.split("\\.");
        customLogger.logToFile(error, "&cBiome "
                + (pathParts[1].equals("levels") ? "&edefault&c" : pathParts[1])
                + " at level " + pathParts[2] + " has an invalid " + invalidObject);
    }
}
