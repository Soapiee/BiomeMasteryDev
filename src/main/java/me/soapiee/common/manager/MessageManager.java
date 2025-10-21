package me.soapiee.common.manager;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
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
        }
        return true;
    }

    public boolean save() {
        try {
            contents.save(file);
        } catch (Exception ex) {
            customLogger.logToFile(ex, ChatColor.RED + "Could not add new fields to messages.yml");
        }
        return true;
    }

    public String get(Message messageEnum) {
        String path = messageEnum.getPath();
        String def = messageEnum.getDefault();

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

    public String getWithPlaceholder(Message messageEnum, String string) {
        return get(messageEnum).replace("%player%", string)
                .replace("%sign_ID%", string)
                .replace("%game_ID%", string)
                .replace("%loc_ID%", string)
                .replace("%task_message%", string.replaceFirst(("(\\W)(\\D)"), ""))
                .replace("%question%", string)
                .replace("%correction_message%\n", (string.isEmpty()) ? "" : string + "\n")
                .replace("%winners%", string)
                .replace("%winner%", string);
    }

    public String getWithPlaceholder(Message messageEnum, int integer) {
        String replacement = integer + " second" + (integer == 1 ? "" : "s");

        return get(messageEnum).replace("%countdown%", replacement)
                .replace("%round_countdown%", replacement)
                .replace("%game_ID%", String.valueOf(integer));
    }

}
