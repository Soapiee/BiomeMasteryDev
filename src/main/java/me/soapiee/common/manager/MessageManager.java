package me.soapiee.common.manager;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageManager {

    private final BiomeMastery main;
    private final File file;
    private final YamlConfiguration contents;

    public MessageManager(BiomeMastery main) {
        this.main = main;
        this.file = new File(this.main.getDataFolder(), "messages.yml");
        this.contents = new YamlConfiguration();

        this.load();
    }

    public boolean load() {
        if (!this.file.exists()) {
            this.main.saveResource("messages.yml", false);
        }

        try {
            this.contents.load(this.file);
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(Utils.colour(this.get(Message.RELOADERROR)));
            throw new RuntimeException(ex);
        }
        return true;
    }

    public boolean save() {
        try {
            this.contents.save(file);
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Could not add new fields to messages.yml");
            throw new RuntimeException(ex);
        }
        return true;
    }

    public String get(Message messageEnum) {
        String path = messageEnum.getPath();
        String def = messageEnum.getDefault();

        if (this.contents.isSet(path)) {
            return (this.contents.isList(path)) ? String.join("\n", this.contents.getStringList(path)) : this.contents.getString(path);
        } else {
            if (def.contains("\n")) {
                String[] list;
                list = def.split("\n");
                this.contents.set(path, list);
            } else {
                this.contents.set(path, def);
            }
            this.save();
            return def;
        }
    }

    public String getWithPlaceholder(Message messageEnum, String string) {
        return this.get(messageEnum).replace("%player%", string)
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

        return this.get(messageEnum).replace("%countdown%", replacement)
                .replace("%round_countdown%", replacement)
                .replace("%game_ID%", String.valueOf(integer));
    }

}
