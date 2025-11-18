package me.soapiee.common.util;

import lombok.Getter;

public enum Message {

    //                    --->    GENERAL MESSAGES    <---
    PREFIX("messages_prefix", "&5&l[BM]"),
    MUSTBEPLAYERERROR("must_be_player", "&cYou must be a player to use this command"),
    CONSOLEUSAGEERROR("console_usage_error", "&cYou must enter a players name"),
    NOPERMISSION("no_permission", "&cYou do not have permission to use this command"),
    PLAYERNOTFOUND("player_not_found", "&cPlayer not found"),
    DATAERROR("data_error", "&c%player_name%'s data could not be loaded or changed at this time"),
    DATAERRORPLAYER("data_error_player", "&cThere was an error loading/saving your data. Please re-log. "
            + "\nIf this error persists, contact support immediately"),
    INVFULL("player_inventory_full", "&cYour reward dropped on the floor because your inventory is full"),
    ADMINSETLEVEL("admin_set_level", "&eYour &a%biome% &elevel has been set to level &a%level% &eby an admin"),
    ADMINADDEDLEVEL("admin_added_level", "&eAn admin added &a%level_formatted% &eto you &a%biome% &elevel"),
    ADMINREMOVEDLEVEL("admin_removed_level", "&eAn admin removed &c%level_formatted% &efrom your &c%biome% &elevel"),
    ADMINSETPROGRESS("admin_set_progress", "&eYour &a%biome% &elevel progress has been set to &a%progress% &eby an admin"),
    ADMINADDEDPROGRESS("admin_added_progress", "&eAn admin added &a%progress% &eto you &a%biome% &elevel"),
    ADMINREMOVEDPROGRESS("admin_removed_progress", "&eAn admin removed &c%progress% &efrom your &c%biome% &elevel"),
    ADMINRESETALL("admin_reset_all", "&eAn admin reset all of your data"),
    ADMINRESETBIOME("admin_reset_biome", "&eAn admin reset your &c%biome% &edata"),


    //                    --->    PLAYER CMD MESSAGES    <---
    PLAYERHELP("player_help", "#01d54a--------- BiomeMastery Help ---------"
            + "\n#01d54aKey: [] = Optional | <> = Required"
            + "\n#01d54a/%cmd_label% info [player] &7- Shows biome levels"
            + "\n#01d54a/%cmd_label% info <biome> [player] &7- Shows more in-depth details about a particular biome"
            + "\n#01d54a/%cmd_label% reward <biome> <level> &7- Toggles a reward (Used for the multi-use rewards)"),
    INVALIDBIOME("player_invalid_biome", "&c%biome% is not a valid biome"),
    INVALIDNUMBER("player_invalid_number", "&c%input% is not a valid number"),
    LEVELOUTOFBOUNDARY("player_input_outofboundary", "&c%level% must be within 0-%max_level%"),
    BIOMEINFODISABLED("player_biome_disabled", "&cThe biome %biome% is disabled"),
    GUIOPENED("player_gui_opened", "&aGUI Opened"),
    REWARDNOTACHIEVED("player_reward_not_available", "&cYou have no achieved this level yet. You are level %current_level%"),
    BIOMEBASICINFOHEADER("player_biome_info_header", "#01d54a--------- %player_name%s Biome Info ---------"),
    BIOMEBASICINFOFORMAT("player_biome_info_format", "&a%biome% &7[Lvl &a%player_level%&7/%biome_max_level% : &a%player_progress%&7/%target_duration_formatted%]"),
    BIOMEBASICINFOSEPERATOR("player_biome_info_seperator", "-"),
    BIOMEBASICINFOMAX("player_biome_info_max", "&a%biome% &7[Lvl %player_level%/%biome_max_level%]"),
    BIOMEDETAILEDFORMAT("player_biome_details_format", "#01d54a--------- %player_name%s %biome% Biome ---------"
            + "\n&7Level: #01d54a%player_level%&7/%biome_max_level%"
            + "\n&7Progress: #01d54a%player_progress%&7/%target_duration_formatted%"
            + "\n&7Rewards:"),
    BIOMEDETAILEDMAX("player_biome_details_max", "#01d54a--------- %player_name%s %biome% Biome ---------"
            + "\n&7Level: #01d54a%player_level%&7/%biome_max_level%"
            + "\n&7Rewards:"),
    BIOMEREWARDFORMAT("player_biome_reward_max", "#01d54a> &7Lvl %level%: #01d54a%reward% &7- %reward_status%"),
    REWARDUNCLAIMED("reward_status_unclaimed", "&5Unclaimed"),
    REWARDCLAIMED("reward_status_claimed", "&5Claimed"),
    REWARDCLAIMINBIOME("reward_claimable_in_biome", "&5Claimable when in the %biome% biome"),
    REWARDACTIVATE("reward_status_activate", "&5Click to activate"),
    REWARDDEACTIVATE("reward_status_deactivate", "&5Click to de-activate"),
    CMDONCOOLDOWN("command_on_cooldown", "&cYou must wait &e%cooldown% &cbefore you can use this command again"),
    LEVELLEDUP("levelled_up", "&aYou levelled up to level &e%level% &ain the %biome% &e&abiome"),
    REWARDACTIVATED("reward_activated", "&aYou activated the reward &e%reward%"),
    REWARDDEACTIVATED("reward_deactivated", "&aYou de-activated the reward &e%reward%"),
    REWARDSDEACTIVATED("all_rewards_deactivated", "&aAll of your %biome% biome rewards were deactivated because you left the biome"),
    PENDINGREWARDRECIEVED("pending_reward_recieved", "&aYou levelled up to level &e%level% &ain the &e%biome% &abiome whilst you were offline. You've received &e%reward%"),
    REWARDALREADYCLAIMED("reward_already_claimed", "&cYou've already claimed this reward and you can only claim it once."),
    NOTINBIOME("not_inside_biome", "&cYou must be inside the &e%biome% &cbiome to activate the &e%reward% &creward"),


    //                    --->    ADMIN CMD MESSAGES    <---
    UPDATEAVAILABLE("update_available", "&aThere is an update available"),
    ADMINHELP("admin_help", "#01d54a--------- BiomeMastery Admin Help ---------"
            + "\n#01d54aKey: [] = Optional | <> = Required"
            + "\n#01d54a/%cmd_label% reload &7- Reloads the plugin"
            + "\n#01d54a/%cmd_label% list worlds &7- Lists all the enabled worlds"
            + "\n#01d54a/%cmd_label% enable|disable <world> &7- Adds/Removes the world from the config. The server must be restarted for it to take effect"
            + "\n#01d54a/%cmd_label% list biomes &7- Lists all the enabled biomes"
            + "\n#01d54a/%cmd_label% enable|disable <biome> &7- Adds/Removes the biome from the config. The server must be restarted for it to take effect"
            + "\n#01d54a/%cmd_label% setlevel|addlevel|removelevel <player> <biome> <amount> &7- Modify a players biome level"
            + "\n#01d54a/%cmd_label% setprogress|addprogress|removeprogress <player> <biome> <amount> &7- Modify the players progress for their current level"
            + "\n#01d54a/%cmd_label% reset <player> [biome] &7- Clears all of the players data or just for a specified biome"),
    WORLDLISTHEADER("world_list_header", "#01d54a--------- Worlds List ---------"),
    BIOMELISTHEADER("biome_list_header", "#01d54a--------- Biome List ---------"),
    INVALIDWORLDBIOME("invalid_world_biome", "&cYou need to enter a valid World or Biome"),
    WORLDALREADYENABLED("world_already_enabled", "&cThis world is already enabled"),
    WORLDENABLED("world_successfully_enabled", "&aBiomes inside the world \"%world%\" will now be registered. The server must be restarted for it to take effect"),
    WORLDALREADYDISABLED("world_already_disabled", "&cThis world is already disabled"),
    WORLDDISABLED("world_successfully_disabled", "&aBiomes inside the world \"%world%\" will no longer be registered. The server must be restarted for it to take effect"),
    BIOMEALREADYENABLED("biome_already_enabled", "&cThis biome is already enabled"),
    BIOMEENABLED("biome_successfully_enabled", "&aThe %biome% biome had been added to the enabled Biomes list. The server must be restarted for it to take effect"),
    BIOMEALREADYDISABLED("biome_already_disabled", "&cThis biome is already disabled"),
    BIOMEDISABLED("biome_successfully_disabled", "&aThe %biome% biome had been removed from the enabled Biomes list. The server must be restarted for it to take effect"),
    INVALIDNEGNUMBER("invalid_negative_no", "&cYou need to enter a value above 0"),
    ADDERROR("add_error", "&cYou cannot add on to this as &e%player_name% &cis already max level"),
    DISABLEDBIOME("disabled_biome", "&cYou cannot edit player data for a disabled biome"),
    LEVELSET("level_set", "&eYou have set &a%player_name%s level to &a%value% &efor biome &a%biome%"),
    LEVELSETERROR("level_set_error", "&c%player_name%s level could not be set to %value%"),
    LEVELADDED("level_added", "&eYou have added &a%level% &eto &a%player_name%s %biome% level"),
    LEVELADDERROR("level_add_error", "&c%level% could not be added to &e%player_name%"),
    LEVELREMOVED("level_removed", "&eYou have removed &c%level% &efrom &a%player_name%s %biome% &elevel"),
    LEVELREMOVEERROR("level_remove_error", "&c%level% could not be removed from &e%player_name%"),
    PROGRESSSET("progress_set", "&eYou have set &a%player_name%s progress to &a%progress% &efor biome &a%biome%"),
    PROGRESSSETERROR("progress_set_error", "&c%player_name%s progress could not be set to %progress%"),
    PROGRESSSETMAX("progress_set_max", "&eYou set over the maximum time needed to &a%player_name%s %biome% &elevel. They are now max level"),
    PROGRESSADDED("progress_added", "&eYou have added &a%progress% &eto &a%player_name%s %biome% &elevel"),
    PROGRESSADDERROR("progress_add_error", "&c%progress% could not be added to &e%player_name%"),
    PROGRESSADDEDMAX("progress_added_max", "&eYou added over the maximum time needed to &a%player_name%s %biome% &elevel. They are now max level"),
    PROGRESSREMOVED("progress_removed", "&eYou have removed &c%progress% &eof progress from &a%player_name%s %biome% level"),
    PROGRESSREMOVEERROR("progress_remove_error", "&c%progress% of progress could not be removed from &e%player_name%"),
    RESETPLAYERBIOME("reset_player_biome", "&e%player_name%s &adata for the &e%biome% &abiome has been reset"),
    RESETPLAYER("reset_player", "&e%player_name%s &adata has been reset"),
    RELOADSUCCESS("reload_success", "&aSuccessfully reloaded Biome Mastery"),
    RELOADERROR("reload_error", "&cError reloading Biome Mastery"),
    RELOADINPROGRESS("reload_inprogress", "&eReloading configuration...");

    @Getter private final String path;
    @Getter private final String defaultText;

    Message(String path, String defaultText) {
        this.path = path;
        this.defaultText = defaultText;
    }
}
