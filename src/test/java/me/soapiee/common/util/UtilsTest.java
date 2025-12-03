package me.soapiee.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @BeforeEach
    void beforeEach() {

    }

    @AfterEach
    void afterEach() {

    }

    @Test
    void givenTwoWords_whenCapitalise_thenReturnFirstLetterAsCapital() {
        String input = "two words";
        String expectedValue = "Two words";
        String actualValue = Utils.capitalise(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenInteger_whenCapitalise_thenReturnInteger() {
        String input = "3 written words";
        String expectedValue = "3 written words";
        String actualValue = Utils.capitalise(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenCapitals_whenCapitalise_thenReturnFirstLetterAsCapital() {
        String input = "ALLCAPS";
        String expectedValue = "Allcaps";
        String actualValue = Utils.capitalise(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void givenStringWithUnderscore_whenCapitalise_thenReturnFirstLetterAsCapital() {
        String input = "FLOWER_FOREST";
        String expectedValue = "Flower Forest";
        String actualValue = Utils.capitalise(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void given60Seconds_whenCapitalise_thenReturnOneMinute() {
        int input = 60;
        String expectedValue = "1min";
        String actualValue = Utils.formatTargetDuration(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void given3599Seconds_whenCapitalise_thenReturnFiftyNineMinutes() {
        int input = 3599;
        String expectedValue = "59mins";
        String actualValue = Utils.formatTargetDuration(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void given3600Seconds_whenCapitalise_thenReturnOneHour() {
        int input = 3600;
        String expectedValue = "1hr";
        String actualValue = Utils.formatTargetDuration(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void given7200Seconds_whenCapitalise_thenReturnTwoHours() {
        int input = 7200;
        String expectedValue = "2hrs";
        String actualValue = Utils.formatTargetDuration(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void given36000Seconds_whenCapitalise_thenReturnTenHours() {
        int input = 36000;
        String expectedValue = "10hrs";
        String actualValue = Utils.formatTargetDuration(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void given86399Seconds_whenCapitalise_thenReturnOneDay() {
        int input = 86399;
        String expectedValue = "23hrs";
        String actualValue = Utils.formatTargetDuration(input);

        assertEquals(expectedValue, actualValue);
    }
    @Test
    void given86400Seconds_whenCapitalise_thenReturnOneDay() {
        int input = 86400;
        String expectedValue = "1day";
        String actualValue = Utils.formatTargetDuration(input);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void given475200Seconds_whenCapitalise_thenReturnOneDay() {
        int input = 475200; // = 5 and a half days
        String expectedValue = "5days";
        String actualValue = Utils.formatTargetDuration(input);

        assertEquals(expectedValue, actualValue);
    }
}
