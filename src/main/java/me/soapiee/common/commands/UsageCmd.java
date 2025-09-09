package me.soapiee.common.commands;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.DataManager;
import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.PlayerCache;
import me.soapiee.common.util.Utils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsageCmd implements CommandExecutor, TabCompleter {

    private final BiomeMastery main;
    private final DataManager dataManager;
    private final PlayerCache playerCache;
    private final MessageManager messageManager;

    public UsageCmd(BiomeMastery main) {
        this.main = main;
        this.dataManager = main.getDataManager();
        this.playerCache = main.getPlayerCache();
        this.messageManager = main.getMessageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length == 1) {
                switch (args[0]) {
                    case "help":

                        sender.sendMessage(Utils.colour(this.messageManager.get(Message.ADMINRELOADCMDUSAGE)));
                        return true;

                    case "info":
                        PlayerData playerData = dataManager.getPlayerData(player.getUniqueId());

                        for (org.bukkit.block.Biome biome : org.bukkit.block.Biome.values()) {
                            BiomeLevel biomeData = playerData.getBiomeData(biome);
                            sender.sendMessage(Utils.colour("&e" + biome.name() + "'s level: "
                                    + biomeData.getLevel()
                                    + " (" + biomeData.getProgress() + " seconds)"));
                        }
                        return true;
                }
            }
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
                if (sender instanceof Player && sender.hasPermission("eveconomy.admin")) {
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
