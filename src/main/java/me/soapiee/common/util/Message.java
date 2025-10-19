package me.soapiee.common.util;

public enum Message {

    //                    --->    GENERAL MESSAGES    <---
    CONSOLEUSAGEERROR("console_usage_error", "&cYou must be a player to use this command"),
    NOPERMISSION("no_permission", "&cYou do not have permission to use this command"),
    PLAYERNOTFOUND("player_not_found", "&cPlayer not found"),
    DATAERROR("data_error", "&c%player_name%'s data could not be loaded or changed at this time"),
    DATAERRORPLAYER("data_error_player", "&cThere was an error loading/saving your data. Please re-log. "
            + "\nIf this error persists, contact support immediately"),
    INVFULL("player_inventory_full", "&cYour reward dropped on the floor because your inventory is full"),
    ADMINRELOADCMDUSAGE("admin_reload_command_usage", "&cUsage: /tf reload"),
    RELOADSUCCESS("reload_success", "&aSuccessfully reloaded Biome Mastery"),
    RELOADERROR("reload_error", "&cError reloading the messages.yml"),
    RELOADINPROGRESS("reload_inprogress", "&eReloading configuration...");

    public final String path;
    private final String defaultText;

    Message(String path, String defaultText) {
        this.path = path;
        this.defaultText = defaultText;
    }

    public String getPath() {
        return path;
    }

    public String getDefault() {
        return defaultText;
    }
}
