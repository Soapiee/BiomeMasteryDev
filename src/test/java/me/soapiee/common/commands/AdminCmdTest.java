package me.soapiee.common.commands;

import me.soapiee.common.data.PlayerData;
import me.soapiee.common.logic.BiomeLevel;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdminCmdTest {

    private static final PlayerData PLAYER_DATA = mock(PlayerData.class, Mockito.RETURNS_DEEP_STUBS);
    private static final BiomeLevel BIOME_LEVEL = mock(BiomeLevel.class, Mockito.RETURNS_DEEP_STUBS);

    @BeforeEach
    void beforeEach() {
    }

    @Test
    void testReadPlayerDataThatReturnsPlayerData() {
        when(PLAYER_DATA.getBiomeData(eq(any(Biome.class)))).thenReturn(BIOME_LEVEL);

        Set<OfflinePlayer> expectedPlayerList = new HashSet<>(Arrays.asList(offlinePlayers));
        Set<OfflinePlayer> actualPlayerList = PLAYER_CACHE.getList();

        assertEquals(actualPlayerList.size(), 2);
        assertEquals(actualPlayerList, expectedPlayerList);
    }
}
