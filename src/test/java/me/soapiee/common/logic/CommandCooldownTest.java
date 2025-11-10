package me.soapiee.common.logic;

import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class CommandCooldownTest {

    private CommandCooldown commandCooldown;
    private ConsoleCommandSender mockConsole;

    @BeforeEach
    void beforeEach() {
        commandCooldown = new CommandCooldown(5);
        mockConsole = mock(ConsoleCommandSender.class);
    }

    @AfterEach
    void afterEach() {
    }

    @Test
    void givenBiomeLevel_whenInitialised_thenReturnNotNull() {
        assertNotNull(commandCooldown);
    }

    @Test
    void givenUUID_whenGetCooldown_thenReturn5Seconds() {
        commandCooldown.addCooldown(mockConsole);
        assertEquals(5, commandCooldown.getCooldown(mockConsole));
    }

    @Test
    void givenUUID_whenGetCooldown_thenReturnNoCooldown() {
        assertEquals(0, commandCooldown.getCooldown(mockConsole));
    }

    @Test
    void givenNewThreshold_whenUpdateThreshold_thenReturnNewThreshold() {
        commandCooldown.updateThreshold(2);
        commandCooldown.addCooldown(mockConsole);
        assertEquals(2, commandCooldown.getCooldown(mockConsole));
    }
}
