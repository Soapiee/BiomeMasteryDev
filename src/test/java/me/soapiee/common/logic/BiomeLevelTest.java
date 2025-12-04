package me.soapiee.common.logic;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BiomeLevelTest {

    private BiomeLevel biomeLevel;

    @BeforeEach
    void beforeEach() {
        OfflinePlayer mockPlayer = mock(OfflinePlayer.class);
        BiomeData mockBiomeData = mock(SingularData.class);
        biomeLevel = new BiomeLevel(mockPlayer, mockBiomeData);

        when(mockBiomeData.getMaxLevel()).thenReturn(5);
        when(mockBiomeData.getTargetDuration(1)).thenReturn(3600);
        when(mockBiomeData.getTargetDuration(2)).thenReturn(3600);
        when(mockBiomeData.getTargetDuration(3)).thenReturn(3600);
        when(mockBiomeData.getTargetDuration(4)).thenReturn(3600);
        when(mockBiomeData.getTargetDuration(5)).thenReturn(3600);
    }

    @AfterEach
    void afterEach() {
    }

    @Test
    void givenBiomeLevel_whenInitialised_thenReturnNotNull() {
        assertNotNull(biomeLevel);
    }

    @Test
    void given2_whenSetLevel_thenReturnNewLevel() {
        int actualValue = biomeLevel.setLevel(2);
        assertEquals(2, actualValue);
    }

    @Test
    void given0_whenSetLevel_thenReturnNewLevel() {
        biomeLevel.setLevel(4);

        int actualValue = biomeLevel.setLevel(0);
        assertEquals(0, actualValue);
    }

    @Test
    void givenNegativeInteger_whenSetLevel_thenReturnFalse() {
        biomeLevel.setLevel(2);

        int actualValue = biomeLevel.setLevel(-1);
        assertEquals(-1, actualValue);
    }

    @Test
    void given2_whenAddLevel_thenReturnNewLevel() {
        int startLevel = biomeLevel.getLevel();
        int actualValue = biomeLevel.setLevel(startLevel + 2);

        assertEquals(startLevel+2, actualValue);
    }

    @Test
    void givenAboveMaxLevel_whenAddLevel_thenReturnFalse() {
        int startLevel = biomeLevel.getLevel();
        int actualValue = biomeLevel.setLevel(startLevel + 6);

        assertEquals(-1, actualValue);
    }

    @Test
    void given2_whenRemoveLevel_thenReturnNewLevel() {
        biomeLevel.setLevel(3);
        int startLevel = biomeLevel.getLevel();
        int actualValue = biomeLevel.setLevel(startLevel - 2);

        assertEquals(startLevel - 2, actualValue);
    }

    @Test
    void givenBelowMinLevel_whenRemoveLevel_thenReturnFalse() {
        int startLevel = biomeLevel.getLevel();
        int actualValue = biomeLevel.setLevel(startLevel - 6);

        assertEquals(-1, actualValue);
    }
}
