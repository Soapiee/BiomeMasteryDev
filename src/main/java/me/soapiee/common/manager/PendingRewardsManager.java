package me.soapiee.common.manager;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.rewards.PendingReward;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.Message;
import me.soapiee.common.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PendingRewardsManager {

    private final Logger customLogger;
    private final BiomeDataManager biomeDataManager;
    private final MessageManager messageManager;

    private final File file;
    private YamlConfiguration contents;
    private final HashMap<UUID, ArrayList<PendingReward>> pendingRewards;

    public PendingRewardsManager(BiomeMastery main, BiomeDataManager biomeDataManager) {
        customLogger = main.getCustomLogger();
        this.biomeDataManager = biomeDataManager;
        messageManager = main.getMessageManager();
        file = new File(main.getDataFolder() + File.separator + "Data", "pendingrewards.yml");
        contents = new YamlConfiguration();
        pendingRewards = new HashMap<>();

        load();
    }

    private void load() {
        if (!file.exists()) {
            try {
                file.createNewFile();
                contents.save(file);
            } catch (Exception error) {
                customLogger.logToFile(error, ChatColor.RED + "Could not create the pendingrewards file");
            }
            return;
        }

        try {
            contents.load(file);

            for (String key : contents.getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                if (!Bukkit.getOfflinePlayer(uuid).hasPlayedBefore()) continue;

                ArrayList<PendingReward> list = readPendingReward(uuid);
                if (list.isEmpty()) continue;
                addAll(uuid, list);
            }

        } catch (Exception error) {
            customLogger.logToFile(error, ChatColor.RED + "Could not load the pendingrewards file");
        }
    }

    public void save() {
        contents = new YamlConfiguration();
        try {
            for (UUID uuid : pendingRewards.keySet()) {
                int i = 1;
                for (PendingReward reward : get(uuid)) {
                    int level = reward.getLevel();
                    String biome = reward.getBiome().replace(" ", "_");

                    contents.set(uuid + "." + i + ".Level", level);
                    contents.set(uuid + "." + i + ".Biome", biome.toLowerCase());
                    i++;
                }
            }

            contents.save(file);
        } catch (Exception error) {
            customLogger.logToFile(error, ChatColor.RED + "Could not save the pendingrewards file");
        }
    }

    public boolean has(UUID uuid) {
        return pendingRewards.containsKey(uuid);
    }

    public ArrayList<PendingReward> get(UUID uuid) {
        return pendingRewards.get(uuid);
    }

    public void add(UUID uuid, PendingReward pendingReward) {
        ArrayList<PendingReward> rewards = new ArrayList<>();
        if (has(uuid)) rewards = get(uuid);

        rewards.add(pendingReward);

        pendingRewards.put(uuid, rewards);
    }

    public void addAll(UUID uuid, ArrayList<PendingReward> rewards) {
        pendingRewards.put(uuid, rewards);
    }

    public void removeAll(UUID uuid) {
        pendingRewards.remove(uuid);
    }

    public void giveAll(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        for (PendingReward reward : get(uuid)) {
            reward.getReward().give(player);
            player.sendMessage(Utils.colour(messageManager.getWithPlaceholder(
                    Message.PENDINGREWARDRECIEVED, reward.getLevel(), reward.getReward(), reward.getBiome())));
        }
    }

    private ArrayList<PendingReward> readPendingReward(UUID uuid) throws IllegalArgumentException {
        ArrayList<PendingReward> rewardsList = new ArrayList<>();

        for (String key : contents.getConfigurationSection(uuid.toString()).getKeys(false)) {
            int level = contents.getInt(uuid + "." + key + ".Level");
            String biome = contents.getString(uuid + "." + key + ".Biome");

            BiomeData biomeData = biomeDataManager.getBiomeData(biome);
            if (biomeData == null) continue;

            Reward reward = biomeData.getReward(level);
            rewardsList.add(new PendingReward(level, biome, reward));
        }

        return rewardsList;
    }
}
