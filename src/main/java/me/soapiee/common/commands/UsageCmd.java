package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.BiomeData;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.CommandCooldown;
import me.soapiee.common.logic.rewards.EffectType;
import me.soapiee.common.logic.rewards.types.EffectReward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.logic.rewards.types.Reward;
import me.soapiee.common.manager.MessageManager;
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
    private final DataManager dataManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;
    private final Logger customLogger;

    public UsageCmd(BiomeMastery main) {
        this.main = main;
        dataManager = main.getDataManager();
        playerCache = main.getPlayerCache();
        messageManager = main.getMessageManager();
        customLogger = main.getCustomLogger();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // /bm help
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(sender, label);
            return true;
        }

        OfflinePlayer target = getTarget(sender, label, args);
        if (target == null) return true;

        //Check player has data
        if (!dataManager.has(target.getUniqueId())) {
            try {
                PlayerData playerData = new PlayerData(main, target);
                dataManager.add(playerData);
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
            if (biome == null) displayInfo(sender, target);
             else displayBiomeInfo(sender, target, biome);
            return true;
        }

        if (biome == null) {
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.INVALIDBIOME, args[1])));
            return true;
        }

        // /bm reward <biome> <level>
        if (args.length == 3 && args[0].equalsIgnoreCase("reward")) {
            toggleReward((Player) sender, biome, args[2]);
            return true;
        }

        // /bm info <biome> [player]
        if (args.length == 3 && args[0].equalsIgnoreCase("info")) {
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
                sender.sendMessage(Utils.colour(messageManager.get(Message.MUSTBEPLAYERERROR)));
                return null;
            }

            target = (Player) sender;

        } else if (args[0].equalsIgnoreCase("info")) {
            // /bm info
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Utils.colour(messageManager.get(Message.CONSOLEUSAGEERROR)));
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
                    sender.sendMessage(Utils.colour(messageManager.get(Message.PLAYERNOTFOUND)));
                }
            } else {
                sendHelpMessage(sender, label);
                target = null;
            }

        } else if (args[0].equalsIgnoreCase("reward")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Utils.colour(messageManager.get(Message.CONSOLEUSAGEERROR)));
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
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.INVALIDBIOME, value)));
            return null;
        }

        if (!dataManager.isEnabledBiome(biome)) {
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.BIOMEINFODISABLED, value)));
            return null;
        }

        return biome;
    }

    private void updateProgress(OfflinePlayer target) {
        // if player is online, get their location, and update that biomelevel
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();
        if (dataManager.isEnabledBiome(locBiome))
            dataManager.getPlayerData(onlinePlayer.getUniqueId()).getBiomeLevel(locBiome).updateProgress();
    }

    private void toggleReward(Player player, Biome biome, String value) {
        int levelToClaim;
        try {
            levelToClaim = Integer.parseInt(value);
        } catch (NumberFormatException error) {
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.INVALIDNUMBER, value)));
            return;
        }

//        updateProgress(player);
        PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        int currentLevel = biomeLevel.getLevel();
        int maxLevel = dataManager.getBiomeData(biome).getMaxLevel();

        if (levelToClaim > maxLevel) {
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.LEVELOUTOFBOUNDARY, maxLevel)));
            return;
        }

        if (currentLevel < levelToClaim) {
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDNOTACHIEVED, currentLevel)));
            return;
        }

        Reward reward = dataManager.getBiomeData(biome).getReward(levelToClaim);
        if (!reward.isTemporary()) {
            if (hasThisActiveReward(player, reward)) {
                deactivateReward(player, playerData, reward);
                return;
            }

            if (player.getLocation().getBlock().getBiome() == biome) {
                activateReward(player, playerData, reward);
                return;
            }

            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.NOTINBIOME, biome.name(), reward.toString())));

        } else {
            player.sendMessage(Utils.colour(messageManager.get(Message.REWARDALREADYCLAIMED)));
        }
    }

    private boolean hasThisActiveReward(Player player, Reward reward) {
        PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());
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

    private void deactivateReward(Player player, PlayerData playerData, Reward reward) {
        if (reward instanceof PotionReward) {
            ((PotionReward) reward).remove(player);
        }
        if (reward instanceof EffectReward) {
            ((EffectReward) reward).remove(player);
        }

        playerData.clearActiveReward(reward);
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDDEACTIVATED, reward.toString())));
    }

    private void activateReward(Player player, PlayerData playerData, Reward reward) {
        if (reward instanceof PotionReward || reward instanceof EffectReward) {
            playerData.addActiveReward(reward);
        }

        reward.give(player);
        player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDACTIVATED, reward.toString())));
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.PLAYERHELP, label)));
    }

    private void displayInfo(CommandSender sender, OfflinePlayer target) {
        CommandCooldown commandCooldown = dataManager.getCommandCooldown();
        int cooldown = (int) commandCooldown.getCooldown(sender);
        if (cooldown > 0) {
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.CMDONCOOLDOWN, cooldown)));
            return;
        }

        updateProgress(target);
        PlayerData playerData = dataManager.getPlayerData(target.getUniqueId());
        StringBuilder builder = new StringBuilder();

        builder.append(messageManager.getWithPlaceholder(Message.BIOMEBASICINFOHEADER, target.getName())).append("\n");

        int i = 0;

        for (Biome biome : dataManager.getEnabledBiomes()) {
            BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
            BiomeData biomeData = dataManager.getBiomeData(biome);

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

        commandCooldown.addCooldown(sender);
        sender.sendMessage(Utils.colour(builder.toString()));
    }

    private void displayBiomeInfo(CommandSender sender, OfflinePlayer target, Biome biome) {
        CommandCooldown commandCooldown = dataManager.getCommandCooldown();
        int cooldown = (int) commandCooldown.getCooldown(sender);
        if (cooldown > 0) {
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.CMDONCOOLDOWN, cooldown)));
            return;
        }

        updateProgress(target);
        PlayerData playerData = dataManager.getPlayerData(target.getUniqueId());
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        BiomeData biomeData = dataManager.getBiomeData(biome);
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

        commandCooldown.addCooldown(sender);
        sender.sendMessage(Utils.colour(builder.toString()));
    }

    private String getRewardStatus(OfflinePlayer player, BiomeData biomeData, int rewardLevel, int currentLevel) {
        if (currentLevel < rewardLevel) return messageManager.get(Message.REWARDUNCLAIMED);

        Reward reward = biomeData.getReward(rewardLevel);
        if (reward.isTemporary()) return messageManager.get(Message.REWARDCLAIMED);

        if (!player.isOnline())
            return messageManager.getWithPlaceholder(Message.REWARDCLAIMINBIOME, biomeData.getBiome().name());

        Player onlinePlayer = player.getPlayer();

        if (hasThisActiveReward(onlinePlayer, reward)){
            return messageManager.get(Message.REWARDDEACTIVATE);
        }

        Biome targetLocation = onlinePlayer.getLocation().getBlock().getBiome();
        if (targetLocation.name().equalsIgnoreCase(biomeData.getBiome().name()))
            return messageManager.get(Message.REWARDACTIVATE);
        else return messageManager.getWithPlaceholder(Message.REWARDCLAIMINBIOME, biomeData.getBiome().name());
    }

    private void openGUI(Player player) {
        //TODO: Open gui for player
        // updateProgress((Player) sender);
        player.sendMessage(messageManager.get(Message.GUIOPENED));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // /bm info [player]

        //TODO work on refactoring these two commands
        // /bm info <biome> [player]
        // /bm reward <biome> <level>
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.add("help");
                results.add("info");
                results.add("reward");
                break;
            case 2:
                if (args[0].equalsIgnoreCase("help")) break;

                for (final Biome biome : dataManager.getEnabledBiomes()) {
                    results.add(biome.name().toLowerCase());
                }

                if (!args[0].equalsIgnoreCase("info")) break;
                for (final OfflinePlayer player : this.playerCache.getList()) {
                    results.add(player.getName());
                }
                break;
            case 3:
                if (args[0].equalsIgnoreCase("reward")) {
                    Biome biome;
                    try {
                        biome = Biome.valueOf(args[0]);
                    } catch (IllegalArgumentException ignored) {
                        break;
                    }
                    int maxLevel = dataManager.getBiomeData(biome).getMaxLevel();

                    for (int i = 1; i <= maxLevel; i++) {
                        results.add(String.valueOf(i));
                    }
                } else if (args[0].equalsIgnoreCase("info")) {
                    for (final OfflinePlayer player : this.playerCache.getList()) {
                        results.add(player.getName());
                    }
                }
        }
        return results.stream().filter(completion -> completion.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

}
