package me.soapiee.common.manager;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.util.Logger;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CmdCooldownTest {

    private CmdCooldownManager mockCooldownManager;
    private ConsoleCommandSender mockConsole;

    @BeforeEach
    void beforeEach() {
        BiomeMastery mockMain = mock(BiomeMastery.class);
        mockConsole = mock(ConsoleCommandSender.class);
        MessageManager mockMessageManager = mock(MessageManager.class);
        Logger mockLogger = mock(Logger.class);
        DataManager mockDataManager = mock(DataManager.class);
        PlayerDataManager mockPlayerDataManager = mock(PlayerDataManager.class);

        when(mockMain.getMessageManager()).thenReturn(mockMessageManager);
        when(mockMain.getCustomLogger()).thenReturn(mockLogger);
        when(mockMain.getDataManager()).thenReturn(mockDataManager);
        when(mockDataManager.getPlayerDataManager()).thenReturn(mockPlayerDataManager);

        mockCooldownManager = new CmdCooldownManager(mockMain, 5);
    }

    @AfterEach
    void afterEach() {
    }

    @Test
    void givenBiomeLevel_whenInitialised_thenReturnNotNull() {
        assertNotNull(mockCooldownManager);
    }

    @Test
    void givenUUID_whenGetCooldown_thenReturn5Seconds() {
        mockCooldownManager.addCooldown(mockConsole);
        assertEquals(5, mockCooldownManager.getCooldown(mockConsole));
    }

    @Test
    void givenUUID_whenGetCooldown_thenReturnNoCooldown() {
        assertEquals(0, mockCooldownManager.getCooldown(mockConsole));
    }

    @Test
    void givenNewThreshold_whenUpdateThreshold_thenReturnNewThreshold() {
        mockCooldownManager.updateThreshold(2);
        mockCooldownManager.addCooldown(mockConsole);
        assertEquals(2, mockCooldownManager.getCooldown(mockConsole));
    }
}
