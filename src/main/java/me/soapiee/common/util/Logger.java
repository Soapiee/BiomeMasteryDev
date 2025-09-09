package me.soapiee.common.util;

import me.soapiee.common.BiomeMastery;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Logger {

    private final File logFile;
    private final boolean debugMode;

    public Logger(BiomeMastery main) {
        logFile = new File(main.getDataFolder() + File.separator + "logger.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Utils.consoleMsg(ChatColor.RED + "Error creating logger file");
            }
        }

        debugMode = main.getConfig().getBoolean("enabled_debug");
    }

    public void logToFile(Exception error, String string) {
        LogType logType = (error == null) ? LogType.WARNING : LogType.SEVERE;
        if (!string.isEmpty()) Utils.consoleMsg(string);

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(logFile, true), true);
            Date dt = new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = df.format(dt);
            writer.write(time + " [" + logType.name() + "] " + string);
            Utils.consoleMsg(ChatColor.RED + "An error has been added to the logger.log file");
            if (error != null) {
                writer.write(System.lineSeparator());
                error.printStackTrace(writer);
            }
            writer.close();
        } catch (IOException e) {
            Utils.consoleMsg(ChatColor.RED + "There was an error whilst writing to the logger file");
        }
    }

    public void logToPlayer(CommandSender sender, Exception error, String string) {
        logToFile(error, string);

        if (sender instanceof Player)
            if (((Player) sender).isOnline())
                sender.sendMessage(Utils.colour(string));
    }

    enum LogType {
        SEVERE(""),
        WARNING("");

        public final String colour;

        LogType(String colour) {
            this.colour = colour;
        }
    }

}
