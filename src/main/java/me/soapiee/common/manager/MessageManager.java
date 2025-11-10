package me.soapiee.common.manager;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.BiomeData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.rewards.types.Reward;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {

    private final BiomeMastery main;
    private final Logger customLogger;
    private final File file;
    private final YamlConfiguration contents;

    public MessageManager(BiomeMastery main) {
        this.main = main;
        this.customLogger = main.getCustomLogger();
        this.file = new File(main.getDataFolder(), "messages.yml");
        this.contents = new YamlConfiguration();

        this.load(null);
    }

    public boolean load(CommandSender sender) {
        if (!file.exists()) {
            main.saveResource("messages.yml", false);
        }

        try {
            contents.load(file);
        } catch (Exception ex) {
            if (sender != null) {
                customLogger.logToPlayer(sender, ex, get(Message.RELOADERROR));
            }
            return false;
        }
        return true;
    }

    public void save(Message messageEnum) {
        try {
            contents.save(file);
            contents.load(file);
        } catch (Exception ex) {
            customLogger.logToFile(ex, ChatColor.RED + "Could not add the'" + messageEnum.getPath() + "' field to messages.yml");
        }
    }

    public String getPrefix(Message messageEnum){
        if (messageEnum == Message.PLAYERHELP
                || messageEnum == Message.ADMINHELP
                || messageEnum == Message.BIOMEBASICINFOHEADER
                || messageEnum == Message.BIOMEBASICINFOFORMAT
                || messageEnum == Message.BIOMEBASICINFOSEPERATOR
                || messageEnum == Message.BIOMEBASICINFOMAX
                || messageEnum == Message.BIOMEDETAILEDFORMAT
                || messageEnum == Message.BIOMEDETAILEDMAX
                || messageEnum == Message.BIOMEREWARDFORMAT
                || messageEnum == Message.REWARDUNCLAIMED
                || messageEnum == Message.REWARDCLAIMED
                || messageEnum == Message.REWARDCLAIMINBIOME
                || messageEnum == Message.REWARDACTIVATE
                || messageEnum == Message.REWARDDEACTIVATE
                || messageEnum == Message.WORLDLISTHEADER
                || messageEnum == Message.BIOMELISTHEADER) return "";

        String path = Message.PREFIX.getPath();
        if (contents.isSet(path)) {
            return contents.getString(path).isEmpty() ? "" : contents.getString(path) + " ";
        }
        else return "";
    }

    public String get(Message messageEnum) {
        String path = messageEnum.getPath();
        String defaultText = messageEnum.getDefaultText();

        if (contents.isSet(path)) {
            return getPrefix(messageEnum) + ((contents.isList(path)) ? String.join("\n", contents.getStringList(path)) : contents.getString(path));
        } else {
            if (defaultText.contains("\n")) {
                String[] list;
                list = defaultText.split("\n");
                contents.set(path, list);
            } else {
                contents.set(path, defaultText);
            }
            save(messageEnum);
            return getPrefix(messageEnum) + defaultText;
        }
    }

    public String getWithPlaceholder(Message messageEnum, String playerName, BiomeData biomeData, BiomeLevel biomeLevel) {
        String formattedBiomeName = Utils.capitalise(biomeData.getBiome().name());
        int currentLevel = biomeLevel.getLevel();
        String formattedTarget = Utils.formatTargetDuration(biomeData.getTargetDuration(currentLevel));
        String formattedProgress = Utils.formatTargetDuration(biomeLevel.getProgress());

        return get(messageEnum).replace("%biome%", formattedBiomeName)
                .replace("%player_name%", playerName)
                .replace("%player_level%", String.valueOf(biomeLevel.getLevel()))
                .replace("%biome_max_level%", String.valueOf(biomeData.getMaxLevel()))
                .replace("%player_progress%", formattedProgress)
                .replace("%target_duration_formatted%", formattedTarget);
    }

    public String getWithPlaceholder(Message messageEnum, String string) {
        return get(messageEnum).replace("%player_name%", string)
                .replace("%cmd_label%", string)
                .replace("%world%", string)
                .replace("%reward%", string)
                .replace("%biome%", Utils.capitalise(string));
    }

    public String getWithPlaceholder(Message messageEnum, String string1, String string2) {
        return get(messageEnum).replace("%player_name%", string2)
                .replace("%reward%", string2)
                .replace("%biome%", string1);
    }

    public String getWithPlaceholder(Message messageEnum, int level, String biomeName) {
        return get(messageEnum).replace("%level%", String.valueOf(level))
                .replace("%biome%", biomeName);
    }

    public String getWithPlaceholder(Message messageEnum, int level, Reward reward, String string) {
        return get(messageEnum).replace("%level%", String.valueOf(level))
                .replace("%reward%", reward.toString())
                .replace("%biome%", string)
                .replace("%reward_status%", string);
    }

    public String getWithPlaceholder(Message messageEnum, int integer) {
        String string = String.valueOf(integer);
        return get(messageEnum).replace("%level%", string)
                .replace("%cooldown%", string + (integer > 1 ? " seconds" : " second"))
                .replace("%current_level%", string)
                .replace("%input%", string);
    }

    public String getWithPlaceholder(Message messageEnum, String playerName, int integer, String biomeName) {
        String string = String.valueOf(integer);
        return get(messageEnum).replace("%level%", string + (integer > 1 ? " levels" : " level"))
                .replace("%value%", string)
                .replace("%progress%", Utils.formatTargetDuration(integer))
                .replace("%biome%", biomeName)
                .replace("%player_name%", playerName);
    }

}
