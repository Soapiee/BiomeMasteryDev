package me.soapiee.common.commands;

import me.soapiee.common.data.DataManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCmd implements CommandExecutor, TabCompleter {

    private final DataManager dataManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;
    private final Logger customLogger;

    public AdminCmd(DataManager dataManager, PlayerCache playerCache, MessageManager messageManager, Logger customLogger) {
        this.dataManager = dataManager;
        this.playerCache = playerCache;
        this.messageManager = messageManager;
        this.customLogger = customLogger;
    }

    private void sendLoadDataError(CommandSender sender, String playerName, Exception e) {
        customLogger.logToPlayer(sender, e, messageManager.getWithPlaceholder(Message.DATAERROR, playerName));
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        //TODO:
        // /abm, /adminbm, /abiome
        // -
        // /abm reload - Reloads the plugin
        // /abm <world> enable|disable - Enables/Disables a world
        // /abm <world> list - Lists the enabled worlds
        // -
        // /abm <biome> enable|disable - Enables/Disables a biome
        // /abm <biome> list - Lists the enabled biomes
        // /abm <biome> <player> setlevel X - Sets the players level
        // /abm <biome> <player> addlevel X - Adds to the players level
        // /abm <biome> <player> removelevel X - Removes levels from the player
        // /abm <biome> <player> setprogress X - Sets the players progress
        // /abm <biome> <player> addprogress X - Adds to the players progress
        // /abm <biome> <player> removeprogress X - Removes progress from the player
        // /abm <biome> <player> reset - Resets the players data for that biome
        // -
        // /abm <biome> <player> <level> - Claims the reward at that level for that player

        if (!sender.hasPermission("admin.command")) {
            sender.sendMessage(Utils.colour(this.messageManager.get(Message.NOPERMISSION)));
            return true;
        }

//            OfflinePlayer target = this.playerCache.getOfflinePlayer(args[1]);

        if (args.length == 1) {

        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> results = new ArrayList<>();

        switch (args.length) {
            case 1:
                results.add("money");
                results.add("multi");
                results.add("help");
                if (sender instanceof Player && sender.hasPermission("biomemastery.admin")) {
                    results.add("reload");
                }
                break;
            case 2:
                for (final OfflinePlayer player : this.playerCache.getList()) {
                    results.add(player.getName().toLowerCase());
                }
                break;
            case 3:
                results.add("set");
                results.add("add");
                results.add("take");
                results.add("subtract");
                results.add("reset");
        }
        return results.stream().filter(completion -> completion.startsWith(args[args.length - 1])).collect(Collectors.toList());
    }

}
