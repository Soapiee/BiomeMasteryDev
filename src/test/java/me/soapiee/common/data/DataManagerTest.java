package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.logic.rewards.types.*;
import me.soapiee.common.manager.CommandCooldownManager;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataManagerTest {

    private FileConfiguration mockConfig;
    private MockedStatic<Bukkit> mockedBukkit;

    private DataManager dataManager;

    @BeforeEach
    void beforeEach() {
        BiomeMastery mockMain = mock(BiomeMastery.class);
        mockConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        MessageManager mockMessageManager = mock(MessageManager.class);
        VaultHook mockVaultHook = mock(VaultHook.class);
        Logger mockLogger = mock(Logger.class);
        CommandCooldownManager mockCooldownManager = mock(CommandCooldownManager.class);
        ConsoleCommandSender mockConsoleSender = mock(ConsoleCommandSender.class);

        // mock BiomeMastery behavior
        when(mockMain.getConfig()).thenReturn(mockConfig);
//        when(mockMain.getMessageManager()).thenReturn(mockMessageManager);
//        when(mockMain.getVaultHook()).thenReturn(mockVaultHook);
//        when(mockMain.getCustomLogger()).thenReturn(mockLogger);
//        when(mockMain.getCooldownManager()).thenReturn(mockCooldownManager);

        // mock Bukkit static methods
        mockedBukkit = Mockito.mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getConsoleSender).thenReturn(mockConsoleSender);

        dataManager = new DataManager(mockConfig, mockMessageManager, mockVaultHook, mockLogger, mockCooldownManager, false);
    }

    @AfterEach
    void afterEach() {
        mockedBukkit.close();
    }

    @Test
    void testDataManagerInitialization() {
        assertNotNull(dataManager);
    }

    @Test
    void givenValidString_whenCreateBiomeWhitelist_thenReturnTrue() {
        List<String> inputList = new ArrayList<>();
        inputList.add("PLAINS");

        List<Biome> actualValue = dataManager.createBiomeWhitelist(Bukkit.getConsoleSender(), inputList);

        assertTrue(actualValue.contains(Biome.PLAINS));
    }

    @Test
    void givenInvalidString_whenCreateBiomeWhitelist_thenReturnFalse() {
        List<String> inputList = new ArrayList<>();
        inputList.add("plainz");

        List<Biome> actualValue = dataManager.createBiomeWhitelist(Bukkit.getConsoleSender(), inputList);

        assertFalse(actualValue.contains(Biome.PLAINS));
    }

    @Test
    void givenValidString_whenCreateBiomeBlacklist_thenReturnFalse() {
        List<String> inputList = new ArrayList<>();
        inputList.add("NETHER_WASTES");

        List<Biome> actualValue = dataManager.createBiomeBlacklist(inputList);

        assertFalse(actualValue.contains(Biome.NETHER_WASTES));
    }

    @Test
    void givenInvalidString_whenCreateBiomeBlacklist_thenReturnTrue() {
        List<String> inputList = new ArrayList<>();
        inputList.add("netherwastes");

        List<Biome> actualValue = dataManager.createBiomeBlacklist(inputList);

        assertTrue(actualValue.contains(Biome.NETHER_WASTES));
    }

    @Test
    void givenTypePotion_whenCreateReward_thenReturnPotionReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("potion");
        when(mockConfig.getString(path + "reward_item")).thenReturn("jump:1");
        when(mockConfig.getString(path + "type", "temporary")).thenReturn("temporary");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        assertInstanceOf(PotionReward.class, actualValue);
    }

    @Test
    void givenTypeEffect_whenCreateReward_thenReturnEffectReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("effect");
        when(mockConfig.getString(path + "reward_item")).thenReturn("night_vision");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        assertInstanceOf(EffectReward.class, actualValue);
    }

    @Test
    void givenTypeCurrency_whenCreateReward_thenReturnCurrencyReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("Currency");
        when(mockConfig.getString(path + "reward_item")).thenReturn("200");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        assertInstanceOf(CurrencyReward.class, actualValue);
    }

    @Test
    void givenTypeExperience_whenCreateReward_thenReturnExperienceReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("Experience");
        when(mockConfig.getString(path + "reward_item")).thenReturn("10");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        assertInstanceOf(ExperienceReward.class, actualValue);
    }

    @Test
    void test() {
        ArrayList<ItemStack> itemList = new ArrayList<>();
        String[] itemParts;
        Material material;
        int amount;

        when(Bukkit.getItemFactory()).thenReturn(mock(ItemFactory.class));

        itemParts = "apple:10".split(":");
        try {
            material = Material.valueOf(itemParts[0].toUpperCase());
            amount = Integer.parseInt(itemParts[1].replace(":", ""));
            itemList.add(new ItemStack(material, amount));
        } catch (NullPointerException | NumberFormatException ignored) {
        }

        assertSame(Material.APPLE, itemList.get(0).getType());
        assertEquals(10, itemList.get(0).getAmount());
    }

    @Test
    void givenTypeItem_whenCreateReward_thenReturnItemReward() {
        when(Bukkit.getItemFactory()).thenReturn(mock(ItemFactory.class));
        String path = "biome.plains.1";
        when(mockConfig.isString(path + "reward_item")).thenReturn(true);
        when(mockConfig.isList(path + "reward_item")).thenReturn(false);
        when(mockConfig.getString(path + "reward_type")).thenReturn("item");
        when(mockConfig.getString(path + "reward_item")).thenReturn("apple:10");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        assertInstanceOf(ItemReward.class, actualValue);
        assertSame(Material.APPLE, ((ItemReward) actualValue).getReward(0).getType());
        assertEquals(10, ((ItemReward) actualValue).getReward(0).getAmount());
    }

    @Test
    void givenTypePermission_whenCreateReward_thenReturnPermissionReward() {
        String path = "biome.plains.1";
        when(mockConfig.isString(path + "reward_item")).thenReturn(false);
        when(mockConfig.isList(path + "reward_item")).thenReturn(true);
        when(mockConfig.getString(path + "reward_type")).thenReturn("Permission");
        ArrayList<String> list = new ArrayList<>();
        list.add("permission1");
        list.add("permission2");
        list.add("permission3");
        when(mockConfig.getStringList(path + "reward_item")).thenReturn(list);

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        assertInstanceOf(PermissionReward.class, actualValue);
    }

    @Test
    void givenTypeCommand_whenCreateReward_thenReturnCommandReward() {
        String path = "biome.plains.1";
        when(mockConfig.isString(path + "reward_item")).thenReturn(true);
        when(mockConfig.isList(path + "reward_item")).thenReturn(false);
        when(mockConfig.getString(path + "reward_type")).thenReturn("command");
        when(mockConfig.getString(path + "reward_item")).thenReturn("give %player% 50");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        assertInstanceOf(CommandReward.class, actualValue);
    }

    @Test
    void givenInvalidType_whenCreateReward_thenReturnNullReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("currency");
        when(mockConfig.getString(path + "reward_item")).thenReturn("abc");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        assertInstanceOf(NullReward.class, actualValue);
    }
}
