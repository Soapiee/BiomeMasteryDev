package me.soapiee.common.util;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerCacheTest {

    private PlayerCache PLAYER_CACHE;

    private static final OfflinePlayer OFFLINE_PLAYER_1 = mock(OfflinePlayer.class, Mockito.RETURNS_DEEP_STUBS);
    private static final OfflinePlayer OFFLINE_PLAYER_2 = mock(OfflinePlayer.class, Mockito.RETURNS_DEEP_STUBS);
    private static OfflinePlayer[] offlinePlayers;

    @BeforeEach
    void beforeEach() {
        when(OFFLINE_PLAYER_1.getName()).thenReturn("Soapiee");
        when(OFFLINE_PLAYER_2.getName()).thenReturn("Beans");

        offlinePlayers = new OfflinePlayer[]{OFFLINE_PLAYER_1, OFFLINE_PLAYER_2};

        // reset before each test to wipe out instance members' data
        PLAYER_CACHE = new PlayerCache(offlinePlayers);
    }

    @AfterEach
    void afterEach() {
    }

    @ParameterizedTest
    @MethodSource("getPlayersArgs")
    void givenOfflinePlayer_WhenGetOfflinePlayer_ThenCorrectlyReturn(String playerName, OfflinePlayer expectedPlayer) {
        assertThat(PLAYER_CACHE.getOfflinePlayer(playerName)).isEqualTo(expectedPlayer);
    }

    @Test
    void givenOfflinePlayers_WhenGetList_ThenReturn() {
        Set<OfflinePlayer> expectedPlaterList = new HashSet<>(Arrays.asList(offlinePlayers));
        Set<OfflinePlayer> actualPlayerList = PLAYER_CACHE.getList();

        assertEquals(actualPlayerList.size(), 2);
        assertEquals(actualPlayerList, expectedPlaterList);
    }

    private static Stream<Arguments> getPlayersArgs() {
        return Stream.of(
                Arguments.of("Soapiee", OFFLINE_PLAYER_1),
                Arguments.of("Beans", OFFLINE_PLAYER_2),
                Arguments.of("NonExistent", null)
        );
    }

}
