package me.soapiee.common.listeners;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import me.soapiee.common.BiomeMastery;
import me.soapiee.common.manager.*;
import me.soapiee.common.util.Logger;
import me.soapiee.common.util.PlayerCache;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PlayerListenerTest {

    private static PlayerListener playerListener;
    private static ConfigManager mockConfigManager;
    private static BiomeDataManager mockBiomeDataManager;

    private static ServerMock server;

    @BeforeAll
    static void beforeAll() {
        server = MockBukkit.mock();
        BiomeMastery mockMain = mock(BiomeMastery.class);
        DataManager mockDataManager = mock(DataManager.class);
        PlayerCache mockPlayerCache = mock(PlayerCache.class);
        PlayerDataManager mockPlayerDataManager = mock(PlayerDataManager.class);
        mockConfigManager = mock(ConfigManager.class);
        mockBiomeDataManager = mock(BiomeDataManager.class);
        Logger mockLogger = mock(Logger.class);
        MessageManager mockMessageManager = mock(MessageManager.class);
        PendingRewardsManager mockPendingRewardsManager = mock(PendingRewardsManager.class);

        when(mockMain.getPlayerCache()).thenReturn(mockPlayerCache);
        when(mockMain.getDataManager()).thenReturn(mockDataManager);
        when(mockMain.getMessageManager()).thenReturn(mockMessageManager);
        when(mockDataManager.getPlayerDataManager()).thenReturn(mockPlayerDataManager);
        when(mockDataManager.getConfigManager()).thenReturn(mockConfigManager);
        when(mockDataManager.getBiomeDataManager()).thenReturn(mockBiomeDataManager);
        when(mockDataManager.getPendingRewardsManager()).thenReturn(mockPendingRewardsManager);
        when(mockMain.getCustomLogger()).thenReturn(mockLogger);

        playerListener = new PlayerListener(mockMain, mockDataManager);
    }

    @AfterAll
    static void afterAll() {
        MockBukkit.unmock();
    }

    @Test
    void testDataManagerInitialization() {
        assertNotNull(playerListener);
    }

    @Test
    void givenValidBiome_whenIsLocEnabled_thenReturnTrue() {
        WorldMock world = server.addSimpleWorld("world");
        Biome biome = Biome.PLAINS;
        when(mockConfigManager.isEnabledWorld(world)).thenReturn(true);
        when(mockConfigManager.isEnabledBiome(biome)).thenReturn(true);

        boolean actualValue = playerListener.isLocEnabled(world, biome);

        assertTrue(actualValue);
    }

    @Test
    void givenValidBiome_whenIsLocEnabled_thenReturnFalseBiome() {
        WorldMock world = server.addSimpleWorld("world");
        Biome biome = Biome.DEEP_COLD_OCEAN;

        when(mockConfigManager.isEnabledWorld(world)).thenReturn(true);
        when(mockConfigManager.isEnabledBiome(biome)).thenReturn(false);

        boolean actualValue = playerListener.isLocEnabled(world, biome);

        assertFalse(actualValue);
    }

    @Test
    void givenValidBiome_whenIsLocEnabled_thenReturnFalseWorld() {
        WorldMock world = server.addSimpleWorld("world");
        Biome biome = Biome.DEEP_COLD_OCEAN;

        when(mockConfigManager.isEnabledWorld(world)).thenReturn(false);
        when(mockConfigManager.isEnabledBiome(biome)).thenReturn(true);

        boolean actualValue = playerListener.isLocEnabled(world, biome);

        assertFalse(actualValue);
    }

    @Test
    void givenIdenticalWorlds_whenWorldHasChanged_thenReturnFalse() {
        WorldMock world = server.addSimpleWorld("world");

        boolean actualValue = playerListener.worldHasChanged(world, world);

        assertFalse(actualValue);
    }

    @Test
    void givenUniqueWorlds_whenWorldHasChanged_thenReturnTrue() {
        WorldMock world = server.addSimpleWorld("world");
        WorldMock nether = server.addSimpleWorld("nether");

        boolean actualValue = playerListener.worldHasChanged(world, nether);

        assertTrue(actualValue);
    }

    @Test
    void givenIdenticalBiomes_whenBiomeHasChanged_thenReturnFalse() {
        Biome prevBiome = Biome.PLAINS;
        Biome newBiome = Biome.PLAINS;

//        when(mockBiomeDataManager.getBiomeData(prevBiome)).thenReturn(mock(SingularData.class));
        boolean actualValue = playerListener.biomeHasChanged(prevBiome, newBiome);

        assertFalse(actualValue);
    }

    @Test
    void givenUniqueBiomes_whenBiomeHasChanged_thenReturnTrue() {
        Biome prevBiome = Biome.OCEAN;
        Biome newBiome = Biome.FOREST;
//        when(mockBiomeDataManager.getBiomeData(prevBiome)).thenReturn(mock(SingularData.class));

        boolean actualValue = playerListener.biomeHasChanged(prevBiome, newBiome);

        assertTrue(actualValue);
    }

    @Test
    void givenUniqueBiomes_whenLocationHasChanged_thenReturnTrue() {
        WorldMock world = server.addSimpleWorld("world");
        Biome prevBiome = Biome.PLAINS;
        Biome newBiome = Biome.FOREST;
//        when(mockBiomeDataManager.getBiomeData(prevBiome)).thenReturn(mock(SingularData.class));

        boolean result = playerListener.locationHasChanged(world, world, prevBiome, newBiome);

        assertTrue(result);
    }

    @Test
    void givenIdenticalBiome_whenLocationHasChanged_thenReturnFalse() {
        WorldMock world = server.addSimpleWorld("world");
        Biome prevBiome = Biome.PLAINS;
        Biome newBiome = Biome.PLAINS;
//        when(mockBiomeDataManager.getBiomeData(prevBiome)).thenReturn(mock(SingularData.class));

        boolean result = playerListener.locationHasChanged(world, world, prevBiome, newBiome);

        assertFalse(result);
    }
}
