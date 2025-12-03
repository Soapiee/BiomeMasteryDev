package me.soapiee.common.manager;

import lombok.Getter;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.effects.Effect;
import me.soapiee.common.logic.effects.EffectType;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class EffectsManager {

    private final BiomeMastery main;
    private final Logger customLogger;
    private final MessageManager messageManager;

    private final File file;
    @Getter private final YamlConfiguration config;

    public EffectsManager(BiomeMastery main) {
        this.main = main;
        customLogger = main.getCustomLogger();
        messageManager = main.getMessageManager();
        file = new File(main.getDataFolder(), "effects.yml");
        config = new YamlConfiguration();

        load(null);
    }

    public boolean load(CommandSender sender) {
        if (!file.exists()) {
            main.saveResource("effects.yml", false);
        }

        try {
            config.load(file);
        } catch (Exception ex) {
            if (sender != null) {
                customLogger.logToPlayer(sender, ex, messageManager.get(Message.RELOADERROR));
            }
            return false;
        }
        return true;
    }

    public void save() {
        try {
            config.save(file);
            config.load(file);
        } catch (Exception ex) {
            customLogger.logToFile(ex, ChatColor.RED + "Could not save the effects.yml");
        }
    }

    public Effect getEffect (EffectType effectType){
        return effectType.getInstance(main, config);
    }
}
