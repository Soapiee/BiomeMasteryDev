package me.soapiee.common.manager;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.BiomeData;
import me.soapiee.common.logic.BiomeLevel;
import me.soapiee.common.logic.rewards.types.Reward;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.ChatColor;
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

        this.load();
    }

    public boolean load() {
        if (!file.exists()) {
            main.saveResource("messages.yml", false);
        }

        try {
            contents.load(file);
        } catch (Exception ex) {
            customLogger.logToFile(ex, get(Message.RELOADERROR));
            return false;
        }
        return true;
    }

    public boolean save() {
        try {
            contents.save(file);
        } catch (Exception ex) {
            customLogger.logToFile(ex, ChatColor.RED + "Could not add new fields to messages.yml");
            return false;
        }
        return true;
    }

    public String get(Message messageEnum) {
        String path = messageEnum.getPath();
        String def = messageEnum.getDefaultText();

        if (contents.isSet(path)) {
            return (contents.isList(path)) ? String.join("\n", contents.getStringList(path)) : contents.getString(path);
        } else {
            if (def.contains("\n")) {
                String[] list;
                list = def.split("\n");
                contents.set(path, list);
            } else {
                contents.set(path, def);
            }
            save();
            return def;
        }
    }

    public String getWithPlaceholder(Message messageEnum, BiomeData biomeData, BiomeLevel biomeLevel) {
        String formattedBiomeName = Utils.capitalise(biomeData.getBiome().name());
        int currentLevel = biomeLevel.getLevel();
        String formattedTarget = Utils.formatTargetDuration(biomeData.getTargetDuration(currentLevel));
        String formattedProgress = Utils.formatTargetDuration(biomeLevel.getProgress());

        return get(messageEnum).replace("%biome%", formattedBiomeName)
                .replace("%player_level%", String.valueOf(biomeLevel.getLevel()))
                .replace("%biome_max_level%", String.valueOf(biomeData.getMaxLevel()))
                .replace("%player_progress%", formattedProgress)
                .replace("%target_duration_formatted%", formattedTarget);
    }

    public String getWithPlaceholder(Message messageEnum, String string) {
        return get(messageEnum).replace("%player%", string)
                .replace("%cmd_label%", string)
                .replace("%biome%", string);
    }

    public String getWithPlaceholder(Message messageEnum, int level, Reward reward, String rewardStatus) {
        return get(messageEnum).replace("%level%", String.valueOf(level))
                .replace("%reward_name%", reward.toString())
                .replace("%reward_status%", rewardStatus);
    }

    public String getWithPlaceholder(Message messageEnum, int integer) {
        String replacement = integer + " second" + (integer == 1 ? "" : "s");

        return get(messageEnum).replace("%level%", String.valueOf(integer))
//                .replace("%round_countdown%", replacement)
                .replace("%example%", String.valueOf(integer))
                .replace("%level%", String.valueOf(integer));
    }

}
