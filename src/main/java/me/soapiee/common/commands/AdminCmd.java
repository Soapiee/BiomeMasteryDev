package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.effects.Effect;
import me.soapiee.common.logic.effects.EffectType;
import me.soapiee.common.logic.events.LevelUpEvent;
import me.soapiee.common.logic.rewards.PendingReward;
import me.soapiee.common.logic.rewards.types.EffectReward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.manager.*;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AdminCmd implements CommandExecutor, TabCompleter {

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final PlayerDataManager playerDataManager;
    private final BiomeDataManager biomeDataManager;
    private final ConfigManager configManager;
    private final EffectsManager effectsManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;
    private final Logger customLogger;
    private final PendingRewardsManager pendingRewardsManager;

    public AdminCmd(BiomeMastery main) {
        this.main = main;
        dataManager = main.getDataManager();
        playerDataManager = dataManager.getPlayerDataManager();
        biomeDataManager = dataManager.getBiomeDataManager();
        configManager = dataManager.getConfigManager();
        effectsManager = dataManager.getEffectsManager();
        playerCache = main.getPlayerCache();
        messageManager = main.getMessageManager();
        customLogger = main.getCustomLogger();
        pendingRewardsManager = dataManager.getPendingRewardsManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof CommandBlock) return true;
        if (!checkPermission(sender, "biomemastery.admin")) return true;

        //DEBUG for effects
//        if (args[0].equalsIgnoreCase("effect")) {
//            if (!(sender instanceof Player)) return true;
//            Player player = ((Player) sender).getPlayer();
//
//            EffectReward reward = new EffectReward(main, playerDataManager, effectsManager, EffectType.LAVASWIMMER, true);
//            Effect effect = reward.getEffect();
//
//            if (effect.isActive(player)) {
//                reward.remove(player);
//                sendMessage(sender, "&cEffect de-activated");
//            } else {
//                reward.give(player);
//            }
//
//            return true;
//        }

        // /abm reload
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!checkPermission(sender, "biomemastery.reload")) return true;
                reload(sender);
                return true;
            }

            sendHelpMessage(sender, label);
            return true;
        }

        // /abm reset <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            resetPlayer(sender, playerCache.getOfflinePlayer(args[1]));
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
                sendMessage(sender, messageManager.get(Message.INVALIDWORLDBIOME));
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
        if (args.length < 2 || args.length > 4) {
            sendHelpMessage(sender, label);
            return true;
        }

        // /abm reset <player> <biome>
        // /abm set|add|remove <player> <biome> X - Sets the players level
        OfflinePlayer target = playerCache.getOfflinePlayer(args[1]);
        if (target == null) {
            sendMessage(sender, messageManager.get(Message.PLAYERNOTFOUND));
            return true;
        }

        if (!playerDataManager.has(target.getUniqueId())) {
            try {
                PlayerData playerData = new PlayerData(main, target);
                playerDataManager.add(playerData);
            } catch (IOException | SQLException error) {
                customLogger.logToPlayer(sender, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                return true;
            }
        }

        Biome inputBiome = validateBiome(sender, args[2]);
        if (inputBiome == null) return true;

        if (!configManager.isEnabledBiome(inputBiome)) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.DISABLEDBIOME, args[2]));
            return true;
        }

        BiomeLevel biomeLevel = playerDataManager.getPlayerData(target.getUniqueId()).getBiomeLevel(inputBiome);

        // /abm reset <player> <biome>
        if (args.length == 3) {
            if (!args[0].equalsIgnoreCase("reset")) {
                sendHelpMessage(sender, label);
                return true;
            }

            resetBiome(sender, target, biomeLevel);
            return true;
        }

        int inputValue;
        try {
            inputValue = Integer.parseInt(args[3]);
        } catch (NumberFormatException error) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDNUMBER, args[3]));
            return true;
        }

        if (inputValue < 0) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDNEGNUMBER, args[3]));
            return true;
        }

        updateProgress(target);
        String argument = args[0].toLowerCase();
        switch (argument) {
            // /abm setlevel <biome> <player> X - Sets the players level
            case "setlevel":
                setLevel(sender, target, biomeLevel, inputValue);
                break;

            // /abm addlevel <biome> <player> X - Adds to the players level
            case "addlevel":
                if (biomeLevel.isMaxLevel()) playerIsMaxLevel(sender, target.getName());
                else addLevel(sender, target, biomeLevel, inputValue);
                break;

            // /abm removelevel <biome> <player> X - Removes levels from the player
            case "removelevel":
                removeLevel(sender, target, biomeLevel, inputValue);
                break;

            // /abm setprogress <biome> <player> X - Sets the players progress
            case "setprogress":
                setProgress(sender, target, biomeLevel, inputValue);
                break;

            // /abm addprogress <biome> <player> X - Adds to the players progress
            case "addprogress":
                if (biomeLevel.isMaxLevel()) playerIsMaxLevel(sender, target.getName());
                else addProgress(sender, target, biomeLevel, inputValue);
                break;

            // /abm removeprogress <biome> <player> X - Removes progress from the player
            case "removeprogress":
                removeProgress(sender, target, biomeLevel, inputValue);
                break;
        }
        return true;
    }

    private void updateProgress(OfflinePlayer target) {
        // if player is online, get their location, and update that biomelevel
        if (!target.isOnline()) return;

        Player onlinePlayer = target.getPlayer();
        Biome locBiome = onlinePlayer.getLocation().getBlock().getBiome();
        if (!configManager.isEnabledBiome(locBiome)) return;

        Biome parentBiome = biomeDataManager.getBiomeData(locBiome).getBiome();
        BiomeLevel biomeLevel = playerDataManager.getPlayerData(onlinePlayer.getUniqueId()).getBiomeLevel(locBiome);
        biomeLevel.updateProgress(parentBiome);
    }

    private void resetPlayer(CommandSender sender, OfflinePlayer target) {
        if (target == null) {
            sendMessage(sender, messageManager.get(Message.PLAYERNOTFOUND));
            return;
        }

        if (!playerDataManager.has(target.getUniqueId())) {
            try {
                playerDataManager.add(new PlayerData(main, target));
            } catch (IOException | SQLException error) {
                customLogger.logToPlayer(sender, error, Utils.colour(messageManager.get(Message.DATAERRORPLAYER)));
                return;
            }
        }

        PlayerData playerData = playerDataManager.getPlayerData(target.getUniqueId());

        for (BiomeLevel biomeLevel : playerData.getBiomeLevels()) {
            biomeLevel.reset();
        }

        sendMessage(sender, messageManager.getWithPlaceholder(Message.RESETPLAYER, target.getName()));
        if (target.isOnline()) {
            playerData.clearActiveRewards();
            sendMessage(target.getPlayer(), messageManager.get(Message.ADMINRESETALL));
        }

        pendingRewardsManager.removeAll(target.getUniqueId());
    }

    private void resetBiome(CommandSender sender, OfflinePlayer player, BiomeLevel biomeLevel) {
        String biomeName = biomeLevel.getBiomeName();

        if (player.isOnline()) {
            removeActiveRewards(biomeLevel.getLevel(), 0, player, biomeLevel.getBiome());
            sendMessage(player.getPlayer(), messageManager.getWithPlaceholder(Message.ADMINRESETBIOME, biomeName));
        }

        biomeLevel.reset();
        sendMessage(sender, messageManager.getWithPlaceholder(Message.RESETPLAYERBIOME, biomeName, player.getName()));

        UUID uuid = player.getUniqueId();
        if (pendingRewardsManager.has(uuid))
            removeBiomePendingRewards(uuid, pendingRewardsManager.get(uuid), biomeLevel.getBiome());
    }

    private void removeBiomePendingRewards(UUID uuid, ArrayList<PendingReward> rewards, Biome biome) {
        ArrayList<PendingReward> list = new ArrayList<>();

        for (PendingReward reward : rewards) {
            if (reward.getBiome().equalsIgnoreCase(biome.name())) continue;
            list.add(reward);
        }

        pendingRewardsManager.addAll(uuid, list);
    }

    private void playerIsMaxLevel(CommandSender sender, String targetName) {
        sendMessage(sender, messageManager.getWithPlaceholder(
                Message.ADDERROR, targetName));
    }

    private void setLevel(CommandSender sender, OfflinePlayer player, BiomeLevel biomeLevel, int inputValue) {
        int oldLevel = biomeLevel.getLevel();
        Biome biome = biomeLevel.getBiome();
        String biomeName = biomeLevel.getBiomeName();

        Message message = Message.LEVELSETERROR;
        if (biomeLevel.setLevel(inputValue) != -1) {
            message = Message.LEVELSET;

            if (oldLevel < inputValue) giveRewards(oldLevel, inputValue, player, biomeLevel);
            if (oldLevel > inputValue) {
                removeActiveRewards(oldLevel, inputValue, player, biome);
                UUID uuid = player.getUniqueId();
                if (pendingRewardsManager.has(uuid))
                    removePendingRewards(uuid, pendingRewardsManager.get(uuid), biome, inputValue);
            }

            if (player.isOnline())
                sendMessage(player.getPlayer(), messageManager.getWithPlaceholder(Message.ADMINSETLEVEL, inputValue, biomeName));
        }

        sendMessage(sender, messageManager.getWithPlaceholder(
                message, player.getName(), inputValue, biomeName));
    }

    private void addLevel(CommandSender sender, OfflinePlayer player, BiomeLevel biomeLevel, int inputValue) {
        int oldLevel = biomeLevel.getLevel();
        int newLevel = oldLevel + inputValue;

        Message message = Message.LEVELADDERROR;
        if (inputValue > 0 && biomeLevel.setLevel(newLevel) != -1) {
            message = Message.LEVELADDED;
            giveRewards(oldLevel, newLevel, player, biomeLevel);

            if (player.isOnline())
                sendMessage(player.getPlayer(), messageManager.getWithPlaceholder(Message.ADMINADDEDLEVEL, inputValue, biomeLevel.getBiomeName()));
        }

        sendMessage(sender, messageManager.getWithPlaceholder(
                message, player.getName(), inputValue, biomeLevel.getBiomeName()));
    }

    private void giveRewards(int oldLevel, int newLevel, OfflinePlayer player, BiomeLevel biomeLevel) {
        for (int i = oldLevel; i <= newLevel; i++) {
            if (i == oldLevel) continue;

            LevelUpEvent event = new LevelUpEvent(player, i, biomeLevel);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    private void removeLevel(CommandSender sender, OfflinePlayer player, BiomeLevel biomeLevel, int inputValue) {
        boolean wasMaxLevel = biomeLevel.isMaxLevel();
        int oldLevel = biomeLevel.getLevel();
        int newLevel = biomeLevel.getLevel() - inputValue;

        Message message = Message.LEVELREMOVEERROR;
        if (biomeLevel.setLevel(newLevel) != -1) {
            message = Message.LEVELREMOVED;
            removeActiveRewards(oldLevel, newLevel, player, biomeLevel.getBiome());

            if (player.isOnline())
                sendMessage(player.getPlayer(), messageManager.getWithPlaceholder(Message.ADMINREMOVEDLEVEL, inputValue, biomeLevel.getBiomeName()));
        }

        sendMessage(sender, messageManager.getWithPlaceholder(
                message, player.getName(), inputValue, biomeLevel.getBiomeName()));

        UUID uuid = player.getUniqueId();
        if (pendingRewardsManager.has(uuid))
            removePendingRewards(uuid, pendingRewardsManager.get(uuid), biomeLevel.getBiome(), newLevel);
        if (wasMaxLevel) biomeLevel.setEntryTime(LocalDateTime.now());
    }

    private void removePendingRewards(UUID uuid, ArrayList<PendingReward> rewards, Biome biome, int newLevel) {
        ArrayList<PendingReward> list = new ArrayList<>();

        for (PendingReward reward : rewards) {
            if (reward.getBiome().equalsIgnoreCase(biome.name()) && reward.getLevel() > newLevel) continue;
            list.add(reward);
        }

        pendingRewardsManager.addAll(uuid, list);
    }

    private void removeActiveRewards(int oldLevel, int newLevel, OfflinePlayer player, Biome biome) {
        BiomeData biomeData = biomeDataManager.getBiomeData(biome);
        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());

        for (int i = oldLevel; i > newLevel; i--) {
            Reward reward = biomeData.getReward(i);
            if (player.isOnline()) checkRewardType(player.getPlayer(), playerData, reward);
        }
    }

    private void checkRewardType(Player player, PlayerData playerData, Reward reward) {
        if (reward instanceof PotionReward) {
            ((PotionReward) reward).remove(player);
            playerData.clearActiveReward(reward);
        }

        if (reward instanceof EffectReward) {
            ((EffectReward) reward).remove(player);
            playerData.clearActiveReward(reward);
        }

    }

    private void setProgress(CommandSender sender, OfflinePlayer player, BiomeLevel biomeLevel, int inputValue) {
        Message message = Message.PROGRESSSET;

        long outcome = biomeLevel.setProgress(inputValue);
        if (outcome == -1) message = Message.PROGRESSSETERROR;
        if (outcome == -2) message = Message.PROGRESSSETMAX;

        if (outcome >= 0)
            if (player.isOnline())
                sendMessage(player.getPlayer(), messageManager.getWithPlaceholder(Message.ADMINSETPROGRESS, inputValue, biomeLevel.getBiomeName()));

        sendMessage(sender, messageManager.getWithPlaceholder(
                message, player.getName(), inputValue, biomeLevel.getBiomeName()));
    }

    private void addProgress(CommandSender sender, OfflinePlayer player, BiomeLevel biomeLevel, int inputValue) {
        long newProgress = biomeLevel.getProgress() + inputValue;
        long outcome = biomeLevel.setProgress(newProgress);

        Message message = Message.PROGRESSADDED;
        if (inputValue < 1) message = Message.PROGRESSADDERROR;
        else if (outcome == -1) message = Message.PROGRESSADDERROR;
        else if (outcome == -2) message = Message.PROGRESSADDEDMAX;

        if (outcome >= 0)
            if (player.isOnline())
                sendMessage(player.getPlayer(), messageManager.getWithPlaceholder(Message.ADMINADDEDPROGRESS, inputValue, biomeLevel.getBiomeName()));

        sendMessage(sender, messageManager.getWithPlaceholder(
                message, player.getName(), inputValue, biomeLevel.getBiomeName()));
    }

    private void removeProgress(CommandSender sender, OfflinePlayer player, BiomeLevel biomeLevel, int inputValue) {
        long newProgress = biomeLevel.getProgress() - inputValue;
        long outcome = biomeLevel.setProgress(newProgress);

        Message message = Message.PROGRESSREMOVEERROR;
        if (outcome != -1) message = Message.PROGRESSREMOVED;

        if (outcome >= 0)
            if (player.isOnline())
                sendMessage(player.getPlayer(), messageManager.getWithPlaceholder(Message.ADMINREMOVEDPROGRESS, inputValue, biomeLevel.getBiomeName()));

        sendMessage(sender, messageManager.getWithPlaceholder(
                message, player.getName(), inputValue, biomeLevel.getBiomeName()));
    }

    private void toggleWorld(CommandSender sender, World inputWorld, boolean enable) {
        Message message;
        if (enable) {
            if (configManager.isEnabledWorld(inputWorld)) {
                message = Message.WORLDALREADYENABLED;
            } else {
                saveWorldList(true, inputWorld.getName());
                message = Message.WORLDENABLED;
            }
        } else {
            if (!configManager.isEnabledWorld(inputWorld)) {
                message = Message.WORLDALREADYDISABLED;
            } else {
                saveWorldList(false, inputWorld.getName());
                message = Message.WORLDDISABLED;
            }
        }

        sendMessage(sender, messageManager.getWithPlaceholder(message, inputWorld.getName()));
    }

    private void saveWorldList(boolean enable, String worldString) {
        FileConfiguration config = main.getConfig();
        ArrayList<String> worldList = new ArrayList<>();

        if (!config.isSet("default_biome_settings.enabled_worlds")) {
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
            if (configManager.isEnabledBiome(inputBiome)) message = Message.BIOMEALREADYENABLED;
            else {
                saveBiomeList(true, inputBiome.name());
                message = Message.BIOMEENABLED;
            }
        } else {
            if (!configManager.isEnabledBiome(inputBiome)) message = Message.BIOMEALREADYDISABLED;
            else {
                saveBiomeList(false, inputBiome.name());
                message = Message.BIOMEDISABLED;
            }
        }

        sendMessage(sender, messageManager.getWithPlaceholder(message, inputBiome.name()));
    }

    private void saveBiomeList(boolean enable, String biomeString) {
        FileConfiguration config = main.getConfig();
        boolean whiteList = config.getBoolean("default_biome_settings.use_blacklist_as_whitelist");

        if (!config.isSet("default_biome_settings.biomes_blacklist")) {
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

        for (World world : configManager.getEnabledWorlds()) {
            enabledWorlds.append("\n").append(Utils.capitalise(world.getName())).append(", ");
        }

        try {
            enabledWorlds.deleteCharAt(enabledWorlds.lastIndexOf(","));
        } catch (StringIndexOutOfBoundsException ignored) {
        }

        sendMessage(sender, enabledWorlds.toString());
    }

    private void listBiomes(CommandSender sender) {
        StringBuilder enabledBiomes = new StringBuilder();
        enabledBiomes.append(messageManager.get(Message.BIOMELISTHEADER));

        for (Biome biome : configManager.getEnabledBiomes()) {
            enabledBiomes.append("\n").append(Utils.capitalise(biome.name())).append(", ");
        }

        try {
            enabledBiomes.deleteCharAt(enabledBiomes.lastIndexOf(","));
        } catch (StringIndexOutOfBoundsException ignored) {
        }

        sendMessage(sender, enabledBiomes.toString());
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (!player.hasPermission(permission))
            sendMessage(player, messageManager.get(Message.NOPERMISSION));

        return player.hasPermission(permission);
    }

    private void reload(CommandSender sender) {
        sendMessage(sender, messageManager.get(Message.RELOADINPROGRESS));
        String reloadOutcome = messageManager.get(Message.RELOADSUCCESS);

        boolean errors = false;
        dataManager.reloadData(main, dataManager);
        if (!messageManager.load(sender)) errors = true;

        if (errors) reloadOutcome = messageManager.get(Message.RELOADERROR);

        if (sender instanceof Player) {
            if (reloadOutcome != null)
                Utils.consoleMsg(ChatColor.GOLD + sender.getName() + " " + reloadOutcome.replace("[BM] ", ""));
        }

        sendMessage(sender, reloadOutcome);
    }

    private void sendHelpMessage(CommandSender sender, String label) {
        String message = messageManager.getWithPlaceholder(Message.ADMINHELP, label);
        if (message == null) return;

        sendMessage(sender, message);
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message == null) return;

        if (sender instanceof Player) sender.sendMessage(Utils.colour(message));
        else Utils.consoleMsg(message);
    }

//    =-=-=-=-=-=-=-=-=-=-=-=-= BIOME DATA POST GROUP UPDATE =-=-=-=-=-=-=-=-=-=-=-=-=
    private Biome validateBiome(CommandSender sender, String input){
        if (configManager.isBiomesGrouped())
            if (configManager.getGroupNameAndParentMap().containsKey(input.toLowerCase()))
                return configManager.getGroupNameAndParentMap().get(input);

        Biome biome;
        try {
            biome = Biome.valueOf(input);
        } catch (IllegalArgumentException error) {
            sendMessage(sender, messageManager.getWithPlaceholder(Message.INVALIDBIOME, input));
            return null;
        }

        return biome;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.addAll(Arrays.asList("list", "enable", "disable", "setlevel", "addlevel", "removelevel",
                        "setprogress", "addprogress", "removeprogress", "reset"));

//                results.add("effect");

                if (sender instanceof Player && sender.hasPermission("biomemastery.admin")) {
                    results.add("reload");
                }
                break;

            case 2:
                if (args[0].equalsIgnoreCase("reload")) break;

                if (args[0].equalsIgnoreCase("list")) {
                    results.add("worlds");
                    results.add("biomes");
                    break;
                }

                if (args[0].equalsIgnoreCase("enable")) {
                    Bukkit.getWorlds().forEach(world -> results.add(world.getName()));

                    Arrays.stream(Biome.values()).forEach(biome -> results.add(biome.name().toLowerCase()));
                    break;
                }

                if (args[0].equalsIgnoreCase("disable")) {
                    Bukkit.getWorlds().forEach(world -> results.add(world.getName()));

                    configManager.getEnabledBiomes().forEach(biome -> results.add(biome.name().toLowerCase()));
                    break;
                }

                playerCache.getOfflinePlayers().forEach(player -> results.add(player.getName()));
                break;

            case 3:
                configManager.getEnabledBiomes().forEach(biome -> results.add(biome.name().toLowerCase()));
                results.addAll(configManager.getGroupNameAndParentMap().keySet());
                break;

            case 4:
                if (args[0].equalsIgnoreCase("setlevel")) {
                    Biome biome;
                    try {
                        biome = Biome.valueOf(args[2]);
                    } catch (IllegalArgumentException ignored) {
                        break;
                    }
                    if (!configManager.isEnabledBiome(biome)) break;

                    int maxLevel = biomeDataManager.getBiomeData(biome).getMaxLevel();

                    for (int i = 1; i <= maxLevel; i++) results.add(String.valueOf(i));
                }

        }
        return results.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

}
