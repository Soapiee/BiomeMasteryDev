package me.soapiee.common.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static void consoleMsg(String message) {
        String prefix = "[" + Bukkit.getServer().getPluginManager().getPlugin("BiomeMastery").getDescription().getPrefix() + "]";
        Bukkit.getConsoleSender().sendMessage(colour(prefix + " " + message));
    }

    public static void debugMsg(String playerName, String message) {
        consoleMsg(org.bukkit.ChatColor.YELLOW + "[DEBUG] " + (playerName.isEmpty() ? "" : "@" + playerName + " " ) + message);
    }

    public static String capitalise(String string){
        String[] stringParts = string.toLowerCase().split("_");

        StringBuilder builder = new StringBuilder();
        builder.append(stringParts[0].substring(0,1).toUpperCase()).append(stringParts[0].substring(1));

        if (stringParts.length > 1){
            for (int i = 1; i < stringParts.length; i++){
                builder.append(" ").append(stringParts[i].substring(0,1).toUpperCase()).append(stringParts[i].substring(1));
            }
        }
        return builder.toString();
    }

    public static String formatTargetDuration(long targetDuration){
        double duration = targetDuration;
        String unit = "s";

        if (targetDuration >= 86400){
            duration = targetDuration/86400.0;
            unit = "day";
        } else if (targetDuration >= 3600){
            duration = targetDuration/3600.0;
            unit = "hr";
        } else if (targetDuration >= 60){
            duration = targetDuration/60.0;
            unit = "min";
        }

        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(Math.floor(duration)) + unit + (!unit.equalsIgnoreCase("s") ? (duration < 2 ? "" : "s") : "");
    }

    public static String colour(String message) { // 1.8 and above
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&" + c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static boolean hasFreeSpace(Material type, int amount, Player player) {
        Inventory inv = player.getInventory();
        int items = 0;
        for (ItemStack item : inv.getStorageContents())
            try {
                if (item == null) {
                    items += type.getMaxStackSize();
                } else if (item.getType() == type) {
                    int stackAmount = item.getAmount();
                    items += type.getMaxStackSize() - stackAmount;
                }
            } catch (NullPointerException ignored) {
            }
        return items > amount;
    }

    public static String progressBar(int completed, int max) {
        float percentComplete = (float) completed / max; //(0.1)
        int totalBars = 10;
        int barsFilled = (int) (totalBars * percentComplete); //1

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < barsFilled; i++) {
            s.append(org.bukkit.ChatColor.GREEN).append("■");
        }
        for (int i = 0; i < (totalBars - barsFilled); i++) {
            s.append(org.bukkit.ChatColor.GRAY).append("■");
        }

        return s.toString();
    }

}
