package me.soapiee.common.data;

import me.soapiee.common.BiomeMastery;
import me.soapiee.common.hooks.VaultHook;
import me.soapiee.common.logic.rewards.RewardFactory;
import me.soapiee.common.logic.rewards.types.*;
import me.soapiee.common.manager.MessageManager;
import me.soapiee.common.manager.PlayerDataManager;
import me.soapiee.common.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RewardFactoryTest {

    private FileConfiguration mockConfig;
    private MockedStatic<Bukkit> mockedBukkit;

    private RewardFactory rewardFactory;

    @BeforeEach
    void beforeEach() {
        BiomeMastery mockMain = mock(BiomeMastery.class);
        mockConfig = mock(FileConfiguration.class);
        MessageManager mockMessageManager = mock(MessageManager.class);
        VaultHook mockVaultHook = mock(VaultHook.class);
        Logger mockLogger = mock(Logger.class);
        PlayerDataManager mockPlayerDataManager = mock(PlayerDataManager.class);

        // mock BiomeMastery behavior
        when(mockMain.getConfig()).thenReturn(mockConfig);

        // mock Bukkit static methods
        mockedBukkit = Mockito.mockStatic(Bukkit.class);

        rewardFactory = new RewardFactory(mockConfig, mockLogger, mockVaultHook, mockMessageManager, mockPlayerDataManager);
    }

    @AfterEach
    void afterEach() {
        mockedBukkit.close();
    }

    @Test
    void testrewardFactoryInitialization() {
        assertNotNull(rewardFactory);
    }

    @Test
    void givenTypePotion_whencreate_thenReturnPotionReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("potion");
        when(mockConfig.getString(path + "reward_item")).thenReturn("jump:1");
        when(mockConfig.getString(path + "type", "temporary")).thenReturn("temporary");

        Reward actualValue = rewardFactory.create(path);

        assertInstanceOf(PotionReward.class, actualValue);
    }

    @Test
    void givenTypeEffect_whencreate_thenReturnEffectReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("effect");
        when(mockConfig.getString(path + "reward_item")).thenReturn("night_vision");

        Reward actualValue = rewardFactory.create(path);

        assertInstanceOf(EffectReward.class, actualValue);
    }

    @Test
    void givenTypeCurrency_whencreate_thenReturnCurrencyReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("Currency");
        when(mockConfig.getString(path + "reward_item")).thenReturn("200");

        Reward actualValue = rewardFactory.create(path);

        assertInstanceOf(CurrencyReward.class, actualValue);
    }

    @Test
    void givenTypeExperience_whencreate_thenReturnExperienceReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("Experience");
        when(mockConfig.getString(path + "reward_item")).thenReturn("10");

        Reward actualValue = rewardFactory.create(path);

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
    void givenTypeItem_whencreate_thenReturnItemReward() {
        when(Bukkit.getItemFactory()).thenReturn(mock(ItemFactory.class));
        String path = "biome.plains.1";
        when(mockConfig.isString(path + "reward_item")).thenReturn(true);
        when(mockConfig.isList(path + "reward_item")).thenReturn(false);
        when(mockConfig.getString(path + "reward_type")).thenReturn("item");
        when(mockConfig.getString(path + "reward_item")).thenReturn("apple:10");

        Reward actualValue = rewardFactory.create(path);

        assertInstanceOf(ItemReward.class, actualValue);
        assertSame(Material.APPLE, ((ItemReward) actualValue).getReward(0).getType());
        assertEquals(10, ((ItemReward) actualValue).getReward(0).getAmount());
    }

    @Test
    void givenTypePermission_whencreate_thenReturnPermissionReward() {
        String path = "biome.plains.1";
        when(mockConfig.isString(path + "reward_item")).thenReturn(false);
        when(mockConfig.isList(path + "reward_item")).thenReturn(true);
        when(mockConfig.getString(path + "reward_type")).thenReturn("Permission");
        ArrayList<String> list = new ArrayList<>();
        list.add("permission1");
        list.add("permission2");
        list.add("permission3");
        when(mockConfig.getStringList(path + "reward_item")).thenReturn(list);

        Reward actualValue = rewardFactory.create(path);

        assertInstanceOf(PermissionReward.class, actualValue);
    }

    @Test
    void givenTypeCommand_whencreate_thenReturnCommandReward() {
        String path = "biome.plains.1";
        when(mockConfig.isString(path + "reward_item")).thenReturn(true);
        when(mockConfig.isList(path + "reward_item")).thenReturn(false);
        when(mockConfig.getString(path + "reward_type")).thenReturn("command");
        when(mockConfig.getString(path + "reward_item")).thenReturn("give %player% 50");

        Reward actualValue = rewardFactory.create(path);

        assertInstanceOf(CommandReward.class, actualValue);
    }

    @Test
    void givenInvalidType_whencreate_thenReturnNullReward() {
        String path = "biome.plains.1";
        when(mockConfig.getString(path + "reward_type")).thenReturn("currency");
        when(mockConfig.getString(path + "reward_item")).thenReturn("abc");

        Reward actualValue = rewardFactory.create(path);

        assertInstanceOf(NullReward.class, actualValue);
    }
}
