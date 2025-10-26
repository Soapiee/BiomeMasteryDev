package me.soapiee.common.util;

import lombok.Getter;

public enum Message {

    //                    --->    GENERAL MESSAGES    <---
    CONSOLEUSAGEERROR("console_usage_error", "&cYou must be a player to use this command"),
    NOPERMISSION("no_permission", "&cYou do not have permission to use this command"),
    PLAYERNOTFOUND("player_not_found", "&cPlayer not found"),
    DATAERROR("data_error", "&c%player_name%'s data could not be loaded or changed at this time"),
    DATAERRORPLAYER("data_error_player", "&cThere was an error loading/saving your data. Please re-log. "
            + "\nIf this error persists, contact support immediately"),
    INVFULL("player_inventory_full", "&cYour reward dropped on the floor because your inventory is full"),


    //                    --->    PLAYER CMD MESSAGES    <---
    PLAYERHELP("player_help", "#01d54a--------- BiomeMastery Help ---------"
            + "\n#01d54aKey: [] = Optional | <> = Required"
            + "\n#01d54a/%cmd_label% info [player] &7- Reloads the plugin"
            + "\n#01d54a/%cmd_label% <biome> [player] &7- description"
            + "\n#01d54a/%cmd_label% <biome> claim <level> &7- description"),
    INVALIDBIOME("player_invalid_biome", "&c%biome%"),
    INVALIDLEVEL("player_invalid_level", "&c%level%"),
    BIOMEDISABLED("player_biome_disabled", "&c%biome% disabled"),
    GUIOPENED("player_gui_opened", "&aGUI Opened"),
    REWARDNOTACHIEVED("player_reward_not_available", "&c%current_level%"),
    BIOMEBASICINFOFORMAT("player_biome_info_format", "&a%biome% &7[Lvl &a%player_level%&7/%biome_max_level% : &a%player_progress%&7/%target_duration_formatted%]"),
    BIOMEBASICINFOSEPERATOR("player_biome_info_seperator", "-"),
    BIOMEBASICINFOMAX("player_biome_info_max", "&a%biome% &7[Lvl %player_level%/%biome_max_level%]"),
    BIOMEDETAILEDFORMAT("player_biome_details_format", "#01d54a--------- %biome% Biome ---------"
            + "\n&7Level: #01d54a%player_level%&7/%biome_max_level%"
            + "\n&7Progress: #01d54a%player_progress%&7/%target_duration_formatted%"
            + "\n&7Rewards:"),
    BIOMEDETAILEDMAX("player_biome_details_max", "#01d54a--------- %biome% Biome ---------"
            + "\n&7Level: #01d54a%player_level%&7/%biome_max_level%"
            + "\n&7Rewards:"),
    BIOMEREWARDFORMAT("player_biome_reward_max", "#01d54a> &7Lvl %level%: #01d54a%reward_name% &7- %reward_status%"),


    //                    --->    ADMIN CMD MESSAGES    <---
    ADMINRELOADCMDUSAGE("admin_reload_command_usage", "&cUsage: /tf reload"),
    RELOADSUCCESS("reload_success", "&aSuccessfully reloaded Biome Mastery"),
    RELOADERROR("reload_error", "&cError reloading the messages.yml"),
    RELOADINPROGRESS("reload_inprogress", "&eReloading configuration...");

    @Getter private final String path;
    @Getter private final String defaultText;

    Message(String path, String defaultText) {
        this.path = path;
        this.defaultText = defaultText;
    }
}
