package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.logic.rewards.RewardFactory;
import me.soapiee.common.manager.ConfigManager;
import me.soapiee.common.util.Logger;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigManagerTest {

    private ConfigManager configManager;

    @BeforeEach
    void beforeEach() {
        BiomeMastery mockMain = mock(BiomeMastery.class);
        FileConfiguration mockConfig = mock(FileConfiguration.class);
        RewardFactory mockRewardFactory = mock(RewardFactory.class);
        Logger mockLogger = mock(Logger.class);

        // mock BiomeMastery behavior
        when(mockMain.getConfig()).thenReturn(mockConfig);

        configManager = new ConfigManager(mockConfig, mockLogger);
    }

    @AfterEach
    void afterEach() {
    }

    @Test
    void testDataManagerInitialization() {
        assertNotNull(configManager);
    }

    @Test
    void givenValidString_whenCreateBiomeWhitelist_thenReturnTrue() {
        List<String> inputList = new ArrayList<>();
        inputList.add("PLAINS");

        List<Biome> actualValue = configManager.createBiomeWhitelist(inputList);

        assertTrue(actualValue.contains(Biome.PLAINS));
    }

    @Test
    void givenInvalidString_whenCreateBiomeWhitelist_thenReturnFalse() {
        List<String> inputList = new ArrayList<>();
        inputList.add("plainz");

        List<Biome> actualValue = configManager.createBiomeWhitelist(inputList);

        assertFalse(actualValue.contains(Biome.PLAINS));
    }

    @Test
    void givenValidString_whenCreateBiomeBlacklist_thenReturnFalse() {
        List<String> inputList = new ArrayList<>();
        inputList.add("NETHER_WASTES");

        List<Biome> actualValue = configManager.createBiomeBlacklist(inputList);

        assertFalse(actualValue.contains(Biome.NETHER_WASTES));
    }

    @Test
    void givenInvalidString_whenCreateBiomeBlacklist_thenReturnTrue() {
        List<String> inputList = new ArrayList<>();
        inputList.add("netherwastes");

        List<Biome> actualValue = configManager.createBiomeBlacklist(inputList);

        assertTrue(actualValue.contains(Biome.NETHER_WASTES));
    }
}
