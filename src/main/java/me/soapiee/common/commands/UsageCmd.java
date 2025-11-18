package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.effects.EffectType;
import me.soapiee.common.logic.rewards.types.EffectReward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.logic.rewards.types.Reward;
import me.soapiee.common.manager.*;
import me.soapiee.common.util.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsageCmd implements CommandExecutor, TabCompleter {

    private final BiomeMastery main;
    private final PlayerDataManager playerDataManager;
    private final ConfigManager configManager;
    private final BiomeDataManager biomeDataManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;
    private final Logger customLogger;
    private final CommandCooldownManager cooldownManager;

    public UsageCmd(BiomeMastery main) {
        this.main = main;
        playerDataManager = main.getDataManager().getPlayerDataManager();
        configManager = main.getDataManager().getConfigManager();
        biomeDataManager = main.getDataManager().getBiomeDataManager();
        playerCache = main.getPlayerCache();
        messageManager = main.getMessageManager();
        customLogger = main.getCustomLogger();
        cooldownManager = main.getCooldownManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!checkPermission(sender, "biomemastery.player.command")) return true;

        // /bm help
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender, label);
            return true;
        }

        OfflinePlayer target = getTarget(sender, label, args);
        if (target == null) return true;

        //Check player has data
        if (!playerDataManager.has(target.getUniqueId())) {
            try {
                PlayerData playerData = new PlayerData(main, target);
                playerDataManager.add(playerData);
            } catch (IOException | SQLException error) {
                customLogger.logToPlayer(sender, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                return true;
            }
        }

        // /bm - Opens the GUI
        if (args.length == 0) {
//            TODO:
//             openGUI((Player) sender);
            sendHelpMessage(sender, label);
            return true;
        }

        // /bm info
        if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
            displayInfo(sender, target);
            return true;
        }

        Biome biome = getBiome(sender, args[1]);

        // /bm info <player>
        // /bm info <biome>
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            if (biome == null) {
                if (!checkPermission(sender, "biomemastery.player.others")) return true;
                displayInfo(sender, target);
            } else displayBiomeInfo(sender, target, biome);
            return true;
        }

        if (biome == null) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDBIOME, args[1]));
            return true;
        }

        // /bm reward <biome> <level>
        if (args.length == 3 && args[0].equalsIgnoreCase("reward")) {
            toggleReward((Player) sender, biome, args[2]);
            return true;
        }

        // /bm info <biome> [player]
        if (args.length == 3 && args[0].equalsIgnoreCase("info")) {
            if (!checkPermission(sender, "biomemastery.player.others")) return true;
            displayBiomeInfo(sender, target, biome);
            return true;
        }

        sendHelpMessage(sender, label);
        return true;
    }

    private OfflinePlayer getTarget(CommandSender sender, String label, String[] args) {
        OfflinePlayer target;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, messageManager.get(Message.MUSTBEPLAYERERROR));
                return null;
            }

            target = (Player) sender;

        } else if (args[0].equalsIgnoreCase("info")) {
            // /bm info
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sendMessage(sender, messageManager.get(Message.CONSOLEUSAGEERROR));
                    return null;
                }

                target = (Player) sender;
            }

            // /bm info <player>
            // /bm info <biome>
            else if (args.length == 2) {
                target = playerCache.getOfflinePlayer(args[1]);

                if (target == null) {
                    target = (Player) sender;
                }
            }

            // /bm info <biome> [player]
            else if (args.length == 3) {
                target = playerCache.getOfflinePlayer(args[2]);

                if (target == null) {
                    sendMessage(sender, messageManager.get(Message.PLAYERNOTFOUND));
                }
            } else {
                sendHelpMessage(sender, label);
                target = null;
            }

        } else if (args[0].equalsIgnoreCase("reward")) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, messageManager.get(Message.CONSOLEUSAGEERROR));
                return null;
            }

            target = (Player) sender;
        } else {
            sendHelpMessage(sender, label);
            target = null;
        }

        return target;
    }

    private Biome getBiome(CommandSender sender, String value) {
        Biome biome;
        try {
            biome = Biome.valueOf(value);
        } catch (IllegalArgumentException error) {
            return null;
        }

        if (!configManager.isEnabledBiome(biome)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.BIOMEINFODISABLED, value));
            return null;
        }

        return biome;
    }

    private void updateProgress(OfflinePlayer target) {
        // if player is online, get their location, and update that biomelevel
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();
        if (configManager.isEnabledBiome(locBiome))
            playerDataManager.getPlayerData(onlinePlayer.getUniqueId()).getBiomeLevel(locBiome).updateProgress();
    }

    private void toggleReward(Player player, Biome biome, String value) {
        int levelToClaim;
        try {
            levelToClaim = Integer.parseInt(value);
        } catch (NumberFormatException error) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.INVALIDNUMBER, value));
            return;
        }

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        int currentLevel = biomeLevel.getLevel();
        int maxLevel = biomeDataManager.getBiomeData(biome).getMaxLevel();

        if (levelToClaim > maxLevel) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.LEVELOUTOFBOUNDARY, maxLevel));
            return;
        }

        if (currentLevel < levelToClaim) {
            sendMessage(player, messageManager.getWithPlaceholder(Message.REWARDNOTACHIEVED, currentLevel));
            return;
        }

        Reward reward = biomeDataManager.getBiomeData(biome).getReward(levelToClaim);
        if (!reward.isTemporary()) {
            if (hasThisActiveReward(player, reward)) {
                deactivateReward(player, reward);
                return;
            }

            if (player.getLocation().getBlock().getBiome() == biome) {
                activateReward(player, reward);
                return;
            }

            sendMessage(player, messageManager.getWithPlaceholder(Message.NOTINBIOME, biome.name(), reward.toString()));

        } else {
            sendMessage(player, messageManager.get(Message.REWARDALREADYCLAIMED));
        }
    }

    private boolean hasThisActiveReward(Player player, Reward reward) {
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        if (playerData.hasActiveRewards()) {
            if (reward instanceof PotionReward) {
                PotionEffectType potion = ((PotionReward) reward).getReward();
                return (player.hasPotionEffect(potion));
            }

            if (reward instanceof EffectReward) {
                if (player.getPersistentDataContainer().has(Keys.CUSTOM_EFFECT, PersistentDataType.STRING)) {
                    EffectType effect = ((EffectReward) reward).getReward();
                    String string = player.getPersistentDataContainer().get(Keys.CUSTOM_EFFECT, PersistentDataType.STRING);
                    return (string.equalsIgnoreCase(effect.name()));
                }
            }
        }

        return false;
    }

    private void deactivateReward(Player player, Reward reward) {
        if (reward instanceof PotionReward) {
            ((PotionReward) reward).remove(player);
        }
        if (reward instanceof EffectReward) {
            ((EffectReward) reward).remove(player);
        }

        sendMessage(player, messageManager.getWithPlaceholder(Message.REWARDDEACTIVATED, reward.toString()));
    }

    private void activateReward(Player player, Reward reward) {
        reward.give(player);
        sendMessage(player, messageManager.getWithPlaceholder(Message.REWARDACTIVATED, reward.toString()));
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        String message = messageManager.getWithPlaceholder(Message.PLAYERHELP, label);
        if (message == null) return;

        sendMessage(sender, message);
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.colour(message));
        else Utils.consoleMsg(message);
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (!player.hasPermission(permission))
            sendMessage(player, messageManager.get(Message.NOPERMISSION));

        return player.hasPermission(permission);
    }

    private void displayInfo(CommandSender sender, OfflinePlayer target) {
        int cooldown = (int) cooldownManager.getCooldown(sender);
        if (cooldown > 0) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.CMDONCOOLDOWN, cooldown));
            return;
        }

        updateProgress(target);
        PlayerData playerData = playerDataManager.getPlayerData(target.getUniqueId());
        StringBuilder builder = new StringBuilder();

        builder.append(messageManager.getWithPlaceholder(Message.BIOMEBASICINFOHEADER, target.getName())).append("\n");

        int i = 0;

        for (Biome biome : configManager.getEnabledBiomes()) {
            BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
            BiomeData biomeData = biomeDataManager.getBiomeData(biome);

            if (i > 0 && i % 3 != 0) {
                builder.append(" ")
                        .append(messageManager.get(Message.BIOMEBASICINFOSEPERATOR))
                        .append(" ");
            }
            i++;

            Message message = Message.BIOMEBASICINFOFORMAT;
            if (biomeLevel.getLevel() == biomeData.getMaxLevel()) message = Message.BIOMEBASICINFOMAX;

            builder.append(messageManager.getWithPlaceholder(message, target.getName(), biomeData, biomeLevel));

            if (i % 3 == 0) builder.append("\n");
        }

        cooldownManager.addCooldown(sender);
        sendMessage(sender, builder.toString());
    }

    private void displayBiomeInfo(CommandSender sender, OfflinePlayer target, Biome biome) {
        int cooldown = (int) cooldownManager.getCooldown(sender);
        if (cooldown > 0) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.CMDONCOOLDOWN, cooldown));
            return;
        }

        updateProgress(target);
        PlayerData playerData = playerDataManager.getPlayerData(target.getUniqueId());
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        BiomeData biomeData = biomeDataManager.getBiomeData(biome);
        StringBuilder builder = new StringBuilder();

        Message message = Message.BIOMEDETAILEDFORMAT;
        if (biomeLevel.getLevel() == biomeData.getMaxLevel()) message = Message.BIOMEDETAILEDMAX;

        builder.append(messageManager.getWithPlaceholder(message, target.getName(), biomeData, biomeLevel));

        for (int i = 1; i <= biomeData.getMaxLevel(); i++) {
            Reward reward = biomeData.getReward(i);

            builder.append("\n")
                    .append(messageManager.getWithPlaceholder(Message.BIOMEREWARDFORMAT,
                            i,
                            reward,
                            getRewardStatus(target, biomeData, i, biomeLevel.getLevel())));
        }

        cooldownManager.addCooldown(sender);
        sendMessage(sender, builder.toString());
    }

    private String getRewardStatus(OfflinePlayer player, BiomeData biomeData, int rewardLevel, int currentLevel) {
        if (currentLevel < rewardLevel) {
            String message = messageManager.get(Message.REWARDUNCLAIMED);
            return message == null ? "" : message;
        }

        Reward reward = biomeData.getReward(rewardLevel);
        if (reward.isTemporary()) {
            String message = messageManager.get(Message.REWARDCLAIMED);
            return message == null ? "" : message;
        }

        if (!player.isOnline()) {
            String message = messageManager.getWithPlaceholder(Message.REWARDCLAIMINBIOME, biomeData.getBiome().name());
            return message == null ? "" : message;
        }

        Player onlinePlayer = player.getPlayer();
        if (hasThisActiveReward(onlinePlayer, reward)) {
            String message = messageManager.get(Message.REWARDDEACTIVATE);
            return message == null ? "" : message;
        }

        Biome targetLocation = onlinePlayer.getLocation().getBlock().getBiome();
        String message;
        if (targetLocation.name().equalsIgnoreCase(biomeData.getBiome().name()))
            message = messageManager.get(Message.REWARDACTIVATE);
        else
            message = messageManager.getWithPlaceholder(Message.REWARDCLAIMINBIOME, biomeData.getBiome().name());

        return message == null ? "" : message;
    }

    private void openGUI(Player player) {
        //TODO: Open gui for player
        // updateProgress((Player) sender);
        sendMessage(player, messageManager.get(Message.GUIOPENED));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.add("help");
                results.add("info");
                results.add("reward");
                break;

            case 2:
                if (args[0].equalsIgnoreCase("help")) break;
                configManager.getEnabledBiomes().forEach(biome -> results.add(biome.name().toLowerCase()));

                if (!args[0].equalsIgnoreCase("info")) break;
                playerCache.getOfflinePlayers().forEach(offlinePlayer -> results.add(offlinePlayer.getName()));
                break;

            case 3:
                if (args[0].equalsIgnoreCase("reward")) {
                    Biome biome;
                    try {
                        biome = Biome.valueOf(args[0]);
                    } catch (IllegalArgumentException ignored) {
                        break;
                    }
                    int maxLevel = biomeDataManager.getBiomeData(biome).getMaxLevel();

                    for (int i = 1; i <= maxLevel; i++) results.add(String.valueOf(i));

                } else if (args[0].equalsIgnoreCase("info")) {
                    playerCache.getOfflinePlayers().forEach(offlinePlayer -> results.add(offlinePlayer.getName()));
                }

        }
        return results.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
