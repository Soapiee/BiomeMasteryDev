package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.events.LevelUpEvent;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCmd implements CommandExecutor, TabCompleter {

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;
    private final Logger customLogger;

    public AdminCmd(BiomeMastery main) {
        this.main = main;
        this.dataManager = main.getDataManager();
        this.playerCache = main.getPlayerCache();
        this.messageManager = main.getMessageManager();
        this.customLogger = main.getCustomLogger();
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof CommandBlock) return true;

        if (sender instanceof Player && !sender.hasPermission("biomemastery.admin")) {
            sender.sendMessage(Utils.colour(this.messageManager.get(Message.NOPERMISSION)));
            return true;
        }

        // /abm reload
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                reload(sender);
                return true;
            }

            sendHelpMessage(sender, label);
            return true;
        }

        // /abm reset <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            resetPlayer(sender, args[1]);
            return true;
        }

        // /abm list worlds
        // /abm list biomes
        if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            switch (args[1]) {
                case "worlds":
                    listWorlds(sender);
                    break;
                case "biomes":
                    listBiomes(sender);
                    break;
                default:
                    sendHelpMessage(sender, label);
                    break;
            }
            return true;
        }

        // /abm enable|disable <world> - Enables/Disables a world
        // /abm enable|disable <biome> - Enables/Disables a biome
        if (args.length == 2) {
            World inputWorld = null;
            Biome inputBiome = null;

            try {
                inputWorld = Bukkit.getWorld(args[1]);
                inputBiome = Biome.valueOf(args[1]);
            } catch (IllegalArgumentException ignored) {
            }

            if (inputWorld == null && inputBiome == null) {
                sender.sendMessage(Utils.colour(messageManager.get(Message.INVALIDWORLDBIOME)));
                return true;
            }

            boolean toEnable;
            if (args[0].equalsIgnoreCase("enable")) toEnable = true;
            else if (args[0].equalsIgnoreCase("disable")) toEnable = false;
            else {
                sendHelpMessage(sender, label);
                return true;
            }

            if (inputWorld != null) toggleWorld(sender, inputWorld, toEnable);
            if (inputBiome != null) toggleBiome(sender, inputBiome, toEnable);
            return true;
        }

        // Incorrect command
        if (args.length > 4) {
            sendHelpMessage(sender, label);
            return true;
        }

        // /abm reset <player> <biome>
        // /abm set|add|remove <player> <biome> X - Sets the players level
        OfflinePlayer target = this.playerCache.getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Utils.colour(messageManager.get(Message.PLAYERNOTFOUND)));
            return true;
        }

        if (!dataManager.has(target.getUniqueId())) {
            try {
                PlayerData playerData = new PlayerData(main, target);
                dataManager.add(playerData);
            } catch (IOException | SQLException error) {
                customLogger.logToPlayer(sender, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                return true;
            }
        }

        Biome inputBiome;
        try {
            inputBiome = Biome.valueOf(args[2]);
        } catch (IllegalArgumentException error) {
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.INVALIDBIOME, args[2])));
            return true;
        }

        if (!dataManager.isEnabledBiome(inputBiome)) {
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.DISABLEDBIOME, args[2])));
            return true;
        }

        BiomeLevel biomeLevel = dataManager.getPlayerData(target.getUniqueId()).getBiomeLevel(inputBiome);

        // /abm reset <player> <biome>
        if (args.length == 3) {
            if (!args[0].equalsIgnoreCase("reset")) {
                sendHelpMessage(sender, label);
                return true;
            }

            resetBiome(sender, target.getName(), biomeLevel);
            return true;
        }

        int inputValue;
        try {
            inputValue = Integer.parseInt(args[3]);
        } catch (NumberFormatException error) {
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.INVALIDNUMBER, args[3])));
            return true;
        }

        if (inputValue < 0) {
            sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.INVALIDNEGNUMBER, args[3])));
            return true;
        }

        biomeLevel.updateProgress();
        String argument = args[0].toLowerCase();
        switch (argument) {
            // /abm setlevel <biome> <player> X - Sets the players level
            case "setlevel":
                setLevel(sender, target.getName(), biomeLevel, inputValue);
                break;

            // /abm addlevel <biome> <player> X - Adds to the players level
            case "addlevel":
                if (biomeLevel.isMaxLevel()) playerIsMaxLevel(sender, target.getName());
                else addLevel(sender, target, biomeLevel, inputValue);
                break;

            // /abm removelevel <biome> <player> X - Removes levels from the player
            case "removelevel":
                removeLevel(sender, target.getName(), biomeLevel, inputValue);
                break;

            // /abm setprogress <biome> <player> X - Sets the players progress
            case "setprogress":
                setProgress(sender, target.getName(), biomeLevel, inputValue);
                break;

            // /abm addprogress <biome> <player> X - Adds to the players progress
            case "addprogress":
                if (biomeLevel.isMaxLevel()) playerIsMaxLevel(sender, target.getName());
                else addProgress(sender, target.getName(), biomeLevel, inputValue);
                break;

            // /abm removeprogress <biome> <player> X - Removes progress from the player
            case "removeprogress":
                removeProgress(sender, target.getName(), biomeLevel, inputValue);
                break;
        }
        return true;
    }

    private void resetPlayer(CommandSender sender, String playerName) {
        OfflinePlayer target = this.playerCache.getOfflinePlayer(playerName);
        if (target == null) {
            sender.sendMessage(Utils.colour(messageManager.get(Message.PLAYERNOTFOUND)));
            return;
        }

        if (!dataManager.has(target.getUniqueId())) {
            try {
                dataManager.add(new PlayerData(main, target));
            } catch (IOException | SQLException error) {
                customLogger.logToPlayer(sender, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                return;
            }
        }

        PlayerData playerData = dataManager.getPlayerData(target.getUniqueId());

        for (BiomeLevel biomeLevel : playerData.getBiomeLevels()) {
            biomeLevel.reset();
        }

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.RESETPLAYER, playerName)));
    }

    private void resetBiome(CommandSender sender, String targetName, BiomeLevel biomeLevel) {
        biomeLevel.reset();
        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.RESETPLAYERBIOME, biomeLevel.getBiome(), targetName)));
    }

    private void playerIsMaxLevel(CommandSender sender, String targetName) {
        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(
                Message.ADDERROR, targetName)));
    }

    private void setLevel(CommandSender sender, String targetName, BiomeLevel biomeLevel, int inputValue) {
        Message message = Message.LEVELSET;
        if (biomeLevel.setLevel(inputValue) == -1) message = Message.LEVELSETERROR;

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(
                message, targetName, inputValue, biomeLevel.getBiome())));
    }

    private void addLevel(CommandSender sender, OfflinePlayer player, BiomeLevel biomeLevel, int inputValue) {
        int oldLevel = biomeLevel.getLevel();
        int newLevel = oldLevel + inputValue;

        Message message = Message.LEVELADDERROR;
        if (inputValue > 0 && biomeLevel.setLevel(newLevel) != -1) {
            message = Message.LEVELADDED;

            for (int i = oldLevel; i <= newLevel; i++) {
                if (i == oldLevel) continue;

                LevelUpEvent event = new LevelUpEvent(player, i, biomeLevel);
                Bukkit.getPluginManager().callEvent(event);
            }
        }

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(
                message, player.getName(), inputValue, biomeLevel.getBiome())));
    }

    private void removeLevel(CommandSender sender, String targetName, BiomeLevel biomeLevel, int inputValue) {
        boolean wasMaxLevel = biomeLevel.isMaxLevel();
        int newLevel = biomeLevel.getLevel() - inputValue;

        Message message = Message.LEVELREMOVEERROR;
        if (biomeLevel.setLevel(newLevel) != -1) message = Message.LEVELREMOVED;

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(
                message, targetName, inputValue, biomeLevel.getBiome())));

        if (wasMaxLevel) biomeLevel.setEntryTime(LocalDateTime.now());
    }

    private void setProgress(CommandSender sender, String targetName, BiomeLevel biomeLevel, int inputValue) {
        Message message = Message.PROGRESSSET;

        long outcome = biomeLevel.setProgress(inputValue);
        if (outcome == -1) message = Message.PROGRESSSETERROR;
        if (outcome == -2) message = Message.PROGRESSSETMAX;

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(
                message, targetName, inputValue, biomeLevel.getBiome())));
    }

    private void addProgress(CommandSender sender, String targetName, BiomeLevel biomeLevel, int inputValue) {
        long newProgress = biomeLevel.getProgress() + inputValue;
        long outcome = biomeLevel.setProgress(newProgress);

        Message message = Message.PROGRESSADDED;
        if (inputValue < 1) message = Message.PROGRESSADDERROR;
        else if (outcome == -1) message = Message.PROGRESSADDERROR;
        else if (outcome == -2) message = Message.PROGRESSADDEDMAX;

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(
                message, targetName, inputValue, biomeLevel.getBiome())));
    }

    private void removeProgress(CommandSender sender, String targetName, BiomeLevel biomeLevel, int inputValue) {
        long newProgress = biomeLevel.getProgress() - inputValue;

        Message message = Message.PROGRESSREMOVEERROR;
        if (biomeLevel.setProgress(newProgress) != -1) message = Message.PROGRESSREMOVED;

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(
                message, targetName, inputValue, biomeLevel.getBiome())));
    }

    private void toggleWorld(CommandSender sender, World inputWorld, boolean enable) {
        Message message;
        if (enable) {
            if (dataManager.isEnabledWorld(inputWorld)) {
                message = Message.WORLDALREADYENABLED;
            } else {
                saveWorldList(true, inputWorld.getName());
                message = Message.WORLDENABLED;
            }
        } else {
            if (!dataManager.isEnabledWorld(inputWorld)) {
                message = Message.WORLDALREADYDISABLED;
            } else {
                saveWorldList(false, inputWorld.getName());
                message = Message.WORLDDISABLED;
            }
        }

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(message, inputWorld.getName())));
    }

    private void saveWorldList(boolean enable, String worldString) {
        FileConfiguration config = main.getConfig();
        ArrayList<String> worldList = new ArrayList<>();

        if (!config.isSet("default_biome_settings.enabled_worlds")){
            config.set("default_biome_settings.enabled_worlds", worldList);
        }

        if (enable) worldList.add(worldString);
        for (String world : config.getStringList("default_biome_settings.enabled_worlds")) {
            if (!enable && world.equalsIgnoreCase(worldString)) continue;

            worldList.add(world);
        }

        config.set("default_biome_settings.enabled_worlds", worldList);
        main.saveConfig();
    }

    private void toggleBiome(CommandSender sender, Biome inputBiome, boolean enable) {
        Message message;

        if (enable) {
            if (dataManager.isEnabledBiome(inputBiome)) message = Message.BIOMEALREADYENABLED;
            else {
                saveBiomeList(true, inputBiome.name());
                message = Message.BIOMEENABLED;
            }
        } else {
            if (!dataManager.isEnabledBiome(inputBiome)) message = Message.BIOMEALREADYDISABLED;
            else {
                saveBiomeList(false, inputBiome.name());
                message = Message.BIOMEDISABLED;
            }
        }

        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(message, inputBiome.name())));
    }

    private void saveBiomeList(boolean enable, String biomeString) {
        FileConfiguration config = main.getConfig();
        boolean whiteList = config.getBoolean("default_biome_settings.use_blacklist_as_whitelist");

        if (!config.isSet("default_biome_settings.biomes_blacklist")){
            config.set("default_biome_settings.biomes_blacklist", new ArrayList<>());
        }
        List<String> biomeList = config.getStringList("default_biome_settings.biomes_blacklist");

        if (whiteList) {
            if (enable) biomeList.add(biomeString);
            else biomeList.remove(biomeString);
        } else {
            if (enable) biomeList.remove(biomeString);
            else biomeList.add(biomeString);
        }

        config.set("default_biome_settings.biomes_blacklist", biomeList);
        main.saveConfig();
    }

    private void listWorlds(CommandSender sender) {
        StringBuilder enabledWorlds = new StringBuilder();
        enabledWorlds.append(messageManager.get(Message.WORLDLISTHEADER));

        for (World world : dataManager.getEnabledWorlds()) {
            enabledWorlds.append("\n").append(Utils.capitalise(world.getName())).append(", ");
        }

        try {
            enabledWorlds.deleteCharAt(enabledWorlds.lastIndexOf(","));
        } catch (StringIndexOutOfBoundsException ignored) {
        }

        sender.sendMessage(Utils.colour(enabledWorlds.toString()));
    }

    private void listBiomes(CommandSender sender) {
        StringBuilder enabledBiomes = new StringBuilder();
        enabledBiomes.append(messageManager.get(Message.BIOMELISTHEADER));

        for (Biome biome : dataManager.getEnabledBiomes()) {
            enabledBiomes.append("\n").append(Utils.capitalise(biome.name())).append(", ");
        }

        try {
            enabledBiomes.deleteCharAt(enabledBiomes.lastIndexOf(","));
        } catch (StringIndexOutOfBoundsException ignored) {
        }

        sender.sendMessage(Utils.colour(enabledBiomes.toString()));
    }

    private void reload(CommandSender sender) {
        sender.sendMessage(Utils.colour(messageManager.get(Message.RELOADINPROGRESS)));
        String reloadOutcome = Utils.colour(messageManager.get(Message.RELOADSUCCESS));

//        dataManager.saveAll(false);

        boolean errors = false;
        dataManager.reloadData(main);
        if (!messageManager.load(sender)) errors = true;

        if (errors) reloadOutcome = Utils.colour(messageManager.get(Message.RELOADERROR));

        if (sender instanceof Player) {
            Utils.consoleMsg(ChatColor.GOLD + sender.getName() + " " + reloadOutcome.replace("[BM] ", ""));
        }

        sender.sendMessage(reloadOutcome);
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        sender.sendMessage(Utils.colour(messageManager.getWithPlaceholder(Message.ADMINHELP, label)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.add("list");
                results.add("enable");
                results.add("disable");
                results.add("setlevel");
                results.add("addlevel");
                results.add("removelevel");
                results.add("setprogress");
                results.add("addprogress");
                results.add("removeprogress");
                results.add("reset");
                if (sender instanceof Player && sender.hasPermission("biomemastery.admin")) {
                    results.add("reload");
                }
                break;

            case 2:
                if (args[0].equalsIgnoreCase("list")) {
                    results.add("worlds");
                    results.add("biomes");
                    break;
                }

                if (args[0].equalsIgnoreCase("enable")) {
                    for (final World world : Bukkit.getWorlds()) {
                        results.add(world.getName());
                    }

                    for (final Biome biome : Biome.values()) {
                        results.add(biome.name().toLowerCase());
                    }
                    break;
                }

                if (args[0].equalsIgnoreCase("disable")) {
                    for (final World world : Bukkit.getWorlds()) {
                        results.add(world.getName());
                    }
                    for (final Biome biome : dataManager.getEnabledBiomes()) {
                        results.add(biome.name().toLowerCase());
                    }
                    break;
                }

                for (final OfflinePlayer player : playerCache.getList()) {
                    results.add(player.getName());
                }
                break;

            case 3:
                for (final Biome biome : dataManager.getEnabledBiomes()) {
                    results.add(biome.name().toLowerCase());
                }
                break;

            case 4:
                if (args[0].equalsIgnoreCase("setlevel")){
                    Biome biome;
                    try {
                        biome = Biome.valueOf(args[2]);
                    } catch (IllegalArgumentException ignored) {
                        break;
                    }
                    if (!dataManager.isEnabledBiome(biome)) break;

                    int maxLevel = dataManager.getBiomeData(biome).getMaxLevel();
                    for (int i = 1; i <= maxLevel; i++) {
                        results.add(String.valueOf(i));
                    }
                }

        }
        return results.stream().filter(completion -> completion.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

}
