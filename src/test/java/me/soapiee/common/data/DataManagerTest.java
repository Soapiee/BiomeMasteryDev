package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.data.rewards.types.*;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataManagerTest {

    private FileConfiguration mockConfig;
    private Logger mockLogger;
    private MessageManager mockMessageManager;
    private VaultHook mockVaultHook;

    private DataManager dataManager;

    @BeforeEach
    void beforeEach() {
        BiomeMastery mockMain = mock(BiomeMastery.class);
        mockConfig = mock(org.bukkit.configuration.file.FileConfiguration.class);
        mockLogger = mock(Logger.class);
        mockMessageManager = mock(MessageManager.class);
        mockVaultHook = mock(VaultHook.class);
        ConsoleCommandSender mockConsoleSender = mock(ConsoleCommandSender.class);

        // mock BiomeMastery behavior
        when(mockMain.getConfig()).thenReturn(mockConfig);
        when(mockMain.getCustomLogger()).thenReturn(mockLogger);
        when(mockMain.getMessageManager()).thenReturn(mockMessageManager);
        when(mockMain.getVaultHook()).thenReturn(mockVaultHook);

        // mock Bukkit static methods
        MockedStatic<Bukkit> mockedBukkit = Mockito.mockStatic(Bukkit.class);
        mockedBukkit.when(Bukkit::getConsoleSender).thenReturn(mockConsoleSender);

        dataManager = new DataManager(mockConfig, mockMessageManager, mockVaultHook, mockLogger);
    }

    @Test
    void testDataManagerInitialization() {
        DataManager dataManager = new DataManager(mockConfig, mockMessageManager, mockVaultHook, mockLogger);

        assertNotNull(dataManager);
    }

    @Test
    public void givenValidString_whenCreateBiomeWhitelist_thenReturnTrue() {
        List<String> inputList = new ArrayList<>();
        inputList.add("PLAINS");

        List<Biome> actualValue = dataManager.createBiomeWhitelist(Bukkit.getConsoleSender(), inputList);

        Assertions.assertTrue(actualValue.contains(Biome.PLAINS));
    }

    @Test
    public void givenInvalidString_whenCreateBiomeWhitelist_thenReturnFalse() {
        List<String> inputList = new ArrayList<>();
        inputList.add("plainz");

        List<Biome> actualValue = dataManager.createBiomeWhitelist(Bukkit.getConsoleSender(), inputList);

        Assertions.assertFalse(actualValue.contains(Biome.PLAINS));
    }

    @Test
    public void givenValidString_whenCreateBiomeBlacklist_thenReturnTrue() {
        List<String> inputList = new ArrayList<>();
        inputList.add("NETHER_WASTES");

        List<Biome> actualValue = dataManager.createBiomeBlacklist(inputList);

        Assertions.assertFalse(actualValue.contains(Biome.NETHER_WASTES));
    }

    @Test
    public void givenInvalidString_whenCreateBiomeBlacklist_thenReturnFalse() {
        List<String> inputList = new ArrayList<>();
        inputList.add("netherwastes");

        List<Biome> actualValue = dataManager.createBiomeBlacklist(inputList);

        Assertions.assertTrue(actualValue.contains(Biome.NETHER_WASTES));
    }

    @Test
    public void givenTypePotion_whenCreateReward_thenReturnPotionReward() {
        String path = "test.";
        when(mockConfig.getString(path + "reward_type")).thenReturn("potion");
        when(mockConfig.getString(path + "reward_item")).thenReturn("jump:1");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        Assertions.assertInstanceOf(PotionReward.class, actualValue);
    }

    @Test
    public void givenTypeEffect_whenCreateReward_thenReturnEffectReward() {

    }

    @Test
    public void givenTypeCurrency_whenCreateReward_thenReturnCurrencyReward() {
        String path = "test.";
        when(mockConfig.getString(path + "reward_type")).thenReturn("Currency");
        when(mockConfig.getString(path + "reward_item")).thenReturn("200");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        Assertions.assertInstanceOf(CurrencyReward.class, actualValue);
    }

    @Test
    public void givenTypeExperience_whenCreateReward_thenReturnExperienceReward() {
        String path = "test.";
        when(mockConfig.getString(path + "reward_type")).thenReturn("Experience");
        when(mockConfig.getString(path + "reward_item")).thenReturn("10");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        Assertions.assertInstanceOf(ExperienceReward.class, actualValue);
    }

    @Test
    public void test() {
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

        Assertions.assertTrue(itemList.get(0).getType() == Material.APPLE);
        Assertions.assertTrue(itemList.get(0).getAmount() == 10);
    }

    @Test
    public void givenTypeItem_whenCreateReward_thenReturnItemReward() {
        when(Bukkit.getItemFactory()).thenReturn(mock(ItemFactory.class));
        String path = "test.";
        when(mockConfig.isString(path + "reward_item")).thenReturn(true);
        when(mockConfig.isList(path + "reward_item")).thenReturn(false);
        when(mockConfig.getString(path + "reward_type")).thenReturn("item");
        when(mockConfig.getString(path + "reward_item")).thenReturn("apple:10");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        Assertions.assertInstanceOf(ItemReward.class, actualValue);
        Assertions.assertTrue(((ItemReward) actualValue).getReward(0).getType() == Material.APPLE);
        Assertions.assertTrue(((ItemReward) actualValue).getReward(0).getAmount() == 10);
    }

    @Test
    public void givenTypePermission_whenCreateReward_thenReturnPermissionReward() {
        String path = "test.";
        when(mockConfig.isString(path + "reward_item")).thenReturn(false);
        when(mockConfig.isList(path + "reward_item")).thenReturn(true);
        when(mockConfig.getString(path + "reward_type")).thenReturn("Permission");
        ArrayList<String> list = new ArrayList<>();
        list.add("permission1");
        list.add("permission2");
        list.add("permission3");
        when(mockConfig.getStringList(path + "reward_item")).thenReturn(list);

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        Assertions.assertInstanceOf(PermissionReward.class, actualValue);
    }

    @Test
    public void givenTypeCommand_whenCreateReward_thenReturnCommandReward() {
        String path = "test.";
        when(mockConfig.isString(path + "reward_item")).thenReturn(true);
        when(mockConfig.isList(path + "reward_item")).thenReturn(false);
        when(mockConfig.getString(path + "reward_type")).thenReturn("command");
        when(mockConfig.getString(path + "reward_item")).thenReturn("give %player% 50");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        Assertions.assertInstanceOf(CommandReward.class, actualValue);
    }

    @Test
    public void givenInvalidType_whenCreateReward_thenReturnNullReward() {
        String path = "test.";
        when(mockConfig.getString(path + "reward_type")).thenReturn("currency");
        when(mockConfig.getString(path + "reward_item")).thenReturn("abc");

        Reward actualValue = dataManager.createReward(Bukkit.getConsoleSender(), path);

        Assertions.assertInstanceOf(NullReward.class, actualValue);
    }
}
