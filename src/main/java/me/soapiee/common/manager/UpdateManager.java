package me.soapiee.common.manager;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateManager {

    private URL resourceURL;
    @Getter private UpdateCheckResult updateCheckResult;
    private final ConfigManager configManager;
    private final MessageManager messageManager;

    public UpdateManager(BiomeMastery main, int resourceId) {
        configManager = main.getDataManager().getConfigManager();
        messageManager = main.getMessageManager();

        try {
            resourceURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        } catch (Exception exception) {
            return;
        }

        String currentVersionString = main.getDescription().getVersion();
        String latestVersionString = getLatestVersion();

        if (latestVersionString == null) {
            updateCheckResult = UpdateCheckResult.NO_RESULT;
            return;
        }

        int currentVersion = Integer.parseInt(currentVersionString.replace("v", "").replace(".", ""));
        int latestVersion = Integer.parseInt(getLatestVersion().replace("v", "").replace(".", ""));

        if (currentVersion < latestVersion) updateCheckResult = UpdateCheckResult.OUT_DATED;
        else if (currentVersion == latestVersion) updateCheckResult = UpdateCheckResult.UP_TO_DATE;
        else updateCheckResult = UpdateCheckResult.NO_RESULT;
    }

    public void updateAlert(CommandSender sender) {
        if (getUpdateCheckResult() != UpdateCheckResult.OUT_DATED) return;

        if (configManager.isUpdateNotif()) {
            String message = messageManager.get(Message.UPDATEAVAILABLE);
            if (message == null) return;

            if (sender instanceof ConsoleCommandSender) Utils.consoleMsg(message);
            else sender.sendMessage(message);
        }
    }

    public String getLatestVersion() {
        try {
            URLConnection urlConnection = resourceURL.openConnection();
            return new BufferedReader(new InputStreamReader(urlConnection.getInputStream())).readLine();
        } catch (Exception exception) {
            return null;
        }
    }

    public enum UpdateCheckResult {
        NO_RESULT, OUT_DATED, UP_TO_DATE;
    }

}
