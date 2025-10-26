package me.soapiee.common.commands;

import me.soapiee.common.data.BiomeData;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.rewards.types.Reward;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsageCmd implements CommandExecutor, TabCompleter {

    private final DataManager dataManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;

    public UsageCmd(DataManager dataManager, PlayerCache playerCache, MessageManager messageManager) {
        this.dataManager = dataManager;
        this.playerCache = playerCache;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // /bm, /biome, /biomemastery
        // /bm - Opens the GUI
        // /bm info [player] - Shows the player their stats for all biomes
        // /bm <biome> [player] - Shows more details about the specified biome
        // /bm <biome> claim <level> - Claims the reward at that level.

        if (sender instanceof Player) {
            Player player = (Player) sender;

            // /bm - Opens the GUI
            if (args.length == 0) {
                openGUI(player);
                return true;
            }

            // /bm help
            if (args[0].equalsIgnoreCase("help")) {
                sendHelpMessage(player, label);
                return true;
            }

            // /bm info [player]
            if (args[0].equalsIgnoreCase("info")) {
                Player target = player;

                if (args.length == 2) {
                    target = Bukkit.getPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(Utils.colour(messageManager.get(Message.PLAYERNOTFOUND)));
                        return true;
                    }
                }

                if (args.length > 2) {
                    sendHelpMessage(sender, label);
                    return true;
                }

                displayInfo(player, target);
                return true;
            }

            // /bm <biome> [player]
            // /bm <biome> claim <level>
            Player target = player;

            Biome biome;
            try {
                biome = Biome.valueOf(args[0]);
            } catch (IllegalArgumentException error) {
                player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.INVALIDBIOME, args[0])));
                return true;
            }

            if (!dataManager.isEnabledBiome(biome)) {
                player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.BIOMEDISABLED, args[0])));
                return true;
            }

            // /bm <biome> claim <level>
            if (args.length == 3 && args[1].equalsIgnoreCase("claim")) {
                claimReward(player, biome, args);
                return true;
            }

            // /bm <biome> [player]
            if (args.length == 2) {
                target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    player.sendMessage(Utils.colour(messageManager.get(Message.PLAYERNOTFOUND)));
                    return true;
                }
            }

            displayBiomeInfo(player, target, biome);
            return true;

        }
        sendHelpMessage(sender, label);
        return true;

    }

    private void claimReward(Player player, Biome biome, String[] args) {
        if (args.length != 3) sendHelpMessage(player, "biome");

        int levelToClaim;
        try {
            levelToClaim = Integer.parseInt(args[2]);
        } catch (NumberFormatException error) {
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.INVALIDLEVEL, args[2])));
            return;
        }

        PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        int currentLevel = playerData.getBiomeLevel(biome).getLevel();

        if (currentLevel < levelToClaim) {
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.REWARDNOTACHIEVED, currentLevel)));
            return;
        }

        biomeLevel.getReward().give(player);
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.PLAYERHELP, label)));
    }

    private void displayInfo(Player sender, Player target) {
        PlayerData playerData = dataManager.getPlayerData(target.getUniqueId());
        StringBuilder builder = new StringBuilder();
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

            builder.append(messageManager.getWithPlaceholder(message, biomeData, biomeLevel));

            if (i % 3 == 0) builder.append("\n");
        }

        sender.sendMessage(Utils.colour(builder.toString()));
    }

    private void displayBiomeInfo(Player sender, Player target, Biome biome) {
        PlayerData playerData = dataManager.getPlayerData(target.getUniqueId());
        BiomeLevel biomeLevel = playerData.getBiomeLevel(biome);
        BiomeData biomeData = dataManager.getBiomeData(biome);
        StringBuilder builder = new StringBuilder();

        Message message = Message.BIOMEDETAILEDFORMAT;
        if (biomeLevel.getLevel() == biomeData.getMaxLevel()) message = Message.BIOMEDETAILEDMAX;

        builder.append(messageManager.getWithPlaceholder(message, biomeData, biomeLevel));

        for (int i = 1; i <= biomeData.getMaxLevel(); i++) {
            Reward reward = biomeData.getReward(i);
            builder.append("\n").append(messageManager.getWithPlaceholder(Message.BIOMEREWARDFORMAT, i, reward, getRewardStatus(biomeLevel, i, target)));
        }

        sender.sendMessage(Utils.colour(builder.toString()));
    }

    private String getRewardStatus(BiomeLevel biomeLevel, int rewardLevel, Player player) {
        if (biomeLevel.getLevel() < rewardLevel) return "Unclaimed";

        String rewardStatus = "Claimed";
        Utils.consoleMsg(ChatColor.GREEN.toString() + biomeLevel.getReward().isTemporary());

        if (!biomeLevel.getReward().isTemporary()) {
            Biome location = player.getLocation().getBlock().getBiome();
            if (location.name().equals(biomeLevel.getBiome().name())) rewardStatus = "Claimable";
        }
        return rewardStatus;
    }

    private void openGUI(Player player) {
        //TODO: Open gui for player
        player.sendMessage(messageManager.get(Message.GUIOPENED));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // /bm
        // /bm info [player]
        // /bm <biome> [player]
        // /bm <biome> claim <level>
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.add("info");
                for (final Biome biome : dataManager.getEnabledBiomes()) {
                    results.add(biome.name().toLowerCase());
                }
                break;
            case 2:
                results.add("claim");
                for (final OfflinePlayer player : this.playerCache.getList()) {
                    results.add(player.getName().toLowerCase());
                }
                break;
            case 3:
                if (args[1].equalsIgnoreCase("claim")) {
                    int maxLevel = dataManager.getBiomeData(Biome.valueOf(args[0])).getMaxLevel();

                    for (int i = 1; i <= maxLevel; i++) {
                        results.add(String.valueOf(i));
                    }
                }
        }
        return results.stream().filter(completion -> completion.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

}
