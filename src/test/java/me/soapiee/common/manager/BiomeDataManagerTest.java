package me.soapiee.common.manager;

import me.soapiee.common.logic.BiomeData;
import me.soapiee.common.logic.ChildData;
import me.soapiee.common.logic.ParentData;
import me.soapiee.common.logic.SingularData;
import me.soapiee.common.logic.rewards.RewardFactory;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BiomeDataManagerTest {

    private static BiomeDataManager biomeDataManager;

    private static Biome parentBiome, childBiome, singularBiome;
    private static BiomeData parentData, singularData;

    @BeforeAll
    static void beforeAll() {
        ConfigManager mockConfigManager = mock(ConfigManager.class);
        RewardFactory mockRewardFactory = mock(RewardFactory.class);
        FileConfiguration mockConfig = mock(FileConfiguration.class);

        // mock BiomeMastery behavior
        biomeDataManager = new BiomeDataManager(mockConfigManager, mockRewardFactory, mockConfig);

        parentBiome = Biome.BIRCH_FOREST;
        parentData = mock(ParentData.class);
        biomeDataManager.getBiomeDataMap().put(parentBiome, parentData);

        childBiome = Biome.BIRCH_FOREST_HILLS;
        ChildData childData = mock(ChildData.class);
        biomeDataManager.getBiomeDataMap().put(childBiome, childData);

        singularBiome = Biome.OCEAN;
        singularData = mock(SingularData.class);
        biomeDataManager.getBiomeDataMap().put(singularBiome, singularData);

        when(childData.getParentData()).thenReturn(parentData);
    }

    @Test
    void testDataManagerInitialization() {
        assertNotNull(biomeDataManager);
    }

    @Test
    void givenParentBiome_whenGetBiomeData_thenReturnSameBiome() {
        BiomeData actualResult = biomeDataManager.getBiomeData(parentBiome);

        assertNotNull(actualResult);
        assertEquals(parentData, actualResult);
    }

    @Test
    void givenChildBiome_whenGetBiomeData_thenReturnParentBiome() {
        BiomeData actualResult = biomeDataManager.getBiomeData(childBiome);

        assertNotNull(actualResult);
        assertEquals(parentData, actualResult);
    }

    @Test
    void givenSingularBiome_whenGetBiomeData_thenReturnSameBiome() {
        BiomeData actualResult = biomeDataManager.getBiomeData(singularBiome);

        assertNotNull(actualResult);
        assertEquals(singularData, actualResult);
    }

    @Test
    void givenDisabledBiome_whenGetBiomeData_thenReturnNull() {
        BiomeData actualResult = biomeDataManager.getBiomeData(Biome.SWAMP_HILLS);

        assertNull(actualResult);
    }

    @Test
    void givenStringParentBiome_whenGetBiomeData_thenReturnSameBiome() {
        BiomeData actualResult = biomeDataManager.getBiomeData(parentBiome.name());

        assertNotNull(actualResult);
        assertEquals(parentData, actualResult);
    }

    @Test
    void givenStringChildBiome_whenGetBiomeData_thenReturnParentBiome() {
        BiomeData actualResult = biomeDataManager.getBiomeData(childBiome.toString());

        assertNotNull(actualResult);
        assertEquals(parentData, actualResult);
    }

    @Test
    void givenStringSingularBiome_whenGetBiomeData_thenReturnSameBiome() {
        BiomeData actualResult = biomeDataManager.getBiomeData(singularBiome.name());

        assertNotNull(actualResult);
        assertEquals(singularData, actualResult);
    }

    @Test
    void givenInvalidStringBiome_whenGetBiomeData_thenReturnException() {
        assertThrows(IllegalArgumentException.class, () -> biomeDataManager.getBiomeData("abcdefg"));
    }
    @Test
    void givenStringDisabledBiome_whenGetBiomeData_thenReturnNull() {
        BiomeData actualResult = biomeDataManager.getBiomeData("Plains");

        assertNull(actualResult);
    }
}
