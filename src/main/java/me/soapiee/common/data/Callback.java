package me.soapiee.common.data;

import org.bukkit.OfflinePlayer;

public interface Callback<V> {

    void onQueryDone(OfflinePlayer player, V results, Exception error);
}
