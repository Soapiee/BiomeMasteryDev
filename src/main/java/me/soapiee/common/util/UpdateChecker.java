package me.soapiee.common.util;

import me.soapiee.common.BiomeMastery;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {

    private URL resourceURL;
    private UpdateCheckResult updateCheckResult;

    public UpdateChecker(BiomeMastery main, int resourceId) {
        try {
            this.resourceURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
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
        else updateCheckResult = UpdateCheckResult.UNRELEASED;
    }

    public UpdateCheckResult getUpdateCheckResult() {
        return updateCheckResult;
    }

    public void updateAlert(BiomeMastery main) {
        FileConfiguration config = main.getConfig();

        if (!config.isSet("update_notification")) {
            config.set("update_notification", true);
            main.saveConfig();
        }

        if (getUpdateCheckResult() != UpdateCheckResult.OUT_DATED) return;

        if (config.getBoolean("update_notification"))
            Utils.consoleMsg(ChatColor.GREEN + "There is an update available");
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
        NO_RESULT, OUT_DATED, UP_TO_DATE, UNRELEASED,
    }

}
