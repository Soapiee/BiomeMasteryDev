package me.soapiee.common.hooks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private static Economy economy = null;
    private static Permission permissions = null;

    public VaultHook() {
        this.setupEconomy();
        this.setupPermissions();
    }

    private void setupEconomy() {
        final RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (rsp != null)
            economy = rsp.getProvider();
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);

        if (rsp != null)
            permissions = rsp.getProvider();
    }

    public boolean hasEconomyPlugin() {
        return economy != null;
    }

    public boolean hasPermissionPlugin() {
        return permissions != null;
    }

    public String deposit(OfflinePlayer target, double amount) {
        if (!hasEconomyPlugin())
            throw new UnsupportedOperationException("Vault Economy not found. You need to install vault for this to work properly");

        return economy.depositPlayer(target, amount).errorMessage;
    }

    public Boolean setPermission(OfflinePlayer target, String permission) {
        if (!hasPermissionPlugin())
            throw new UnsupportedOperationException("Vault Permission Plugin not found. You need to install vault for this to work properly");

        return permissions.playerAdd(null, target, permission);
    }

}
