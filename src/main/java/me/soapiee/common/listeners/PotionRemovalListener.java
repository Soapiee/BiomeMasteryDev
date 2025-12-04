package me.soapiee.common.listeners;

import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.rewards.Reward;
import me.soapiee.common.logic.rewards.types.PotionReward;
import me.soapiee.common.manager.PlayerDataManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.ArrayList;
import java.util.UUID;

public class PotionRemovalListener implements Listener {

    private final PlayerDataManager playerDataManager;

    public PotionRemovalListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onHeal(PlayerCommandPreprocessEvent event){
        String cmd = event.getMessage();
        if (!cmd.equalsIgnoreCase("/heal")) return;

        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        if (!hasActivePotions(playerData)) return;
        clearPotionRewards(playerData);
    }

    @EventHandler
    public void onConsumeMilk(PlayerItemConsumeEvent event){
        if (event.getItem().getType() != Material.MILK_BUCKET) return;

        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        if (!hasActivePotions(playerData)) return;
        clearPotionRewards(playerData);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        UUID uuid = event.getEntity().getUniqueId();
        PlayerData playerData = playerDataManager.getPlayerData(uuid);

        if (!hasActivePotions(playerData)) return;
        clearPotionRewards(playerData);
    }

    private boolean hasActivePotions(PlayerData playerData){
        if (!playerData.hasActiveRewards()) return false;

        for (Reward reward : playerData.getActiveRewards()){
            if (reward instanceof PotionReward) return true;
        }
        return false;
    }

    private void clearPotionRewards(PlayerData playerData){
        ArrayList<Reward> activeRewards = new ArrayList<>(playerData.getActiveRewards());

        for (Reward reward : activeRewards){
            if (reward instanceof PotionReward){
                playerData.clearActiveReward(reward);
            }
        }
    }
}
