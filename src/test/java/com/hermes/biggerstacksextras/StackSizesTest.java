package com.hermes.biggerstacksextras;

import com.hermes.biggerstacksextras.config.ModConfig;
import com.hermes.biggerstacksextras.util.StackSizes;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 不启动游戏、只启动原版注册表，验证“哪类物品归谁管”。
 * 这样能确认不是所有物品都被我们改了上限。
 */
class StackSizesTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        try {
            Bootstrap.bootStrap();
        } catch (Throwable ignored) {
            // Forge 的 NetworkHooks 在纯单元测试里没有初始化，会在这里抛错；
            // 物品注册表在这之前已经建好，后面的断言仍然可用。
        }
    }

    @BeforeEach
    void resetDefaults() {
        ModConfig.Cache.bookSize = 16;
        ModConfig.Cache.potionSize = 16;
        ModConfig.Cache.bloodSize = 16;
        ModConfig.Cache.toolSize = 16;
        ModConfig.Cache.ammoMode = "multiplier";
        ModConfig.Cache.ammoMultiplier = 10;
        ModConfig.Cache.ammoFixed = 600;
        ModConfig.Cache.ammoFallback = 64;
        ModConfig.Cache.vanillaMode = "off";
        ModConfig.Cache.gtmMode = "off";
        ModConfig.Cache.ammoListMode = "off";
        ModConfig.Cache.vanillaList = new java.util.LinkedHashSet<>();
        ModConfig.Cache.gtmList = new java.util.LinkedHashSet<>();
        ModConfig.Cache.ammoList = new java.util.LinkedHashSet<>();
        ModConfig.Cache.extraFixed.clear();
        ModConfig.Cache.extraMultiplier.clear();
        ModConfig.Cache.ammoOverrides.clear();
    }

    @Test
    void vanillaToolsStack() {
        assertEquals(16, StackSizes.resolve(new ItemStack(Items.DIAMOND_PICKAXE)));
        assertEquals(16, StackSizes.resolve(new ItemStack(Items.IRON_SWORD)));
        assertEquals(16, StackSizes.resolve(new ItemStack(Items.BOW)));
        assertEquals(16, StackSizes.resolve(new ItemStack(Items.SHIELD)));
    }

    @Test
    void dangerItemsAreForcedToVanilla() {
        assertEquals(1, StackSizes.resolve(new ItemStack(Items.TRIDENT)));
        assertEquals(1, StackSizes.resolve(new ItemStack(Items.CROSSBOW)));
    }

    @Test
    void armorIsNotTouched() {
        assertEquals(-1, StackSizes.resolve(new ItemStack(Items.DIAMOND_CHESTPLATE)));
        assertEquals(-1, StackSizes.resolve(new ItemStack(Items.ELYTRA)));
    }

    @Test
    void booksAndPotions() {
        assertEquals(16, StackSizes.resolve(new ItemStack(Items.ENCHANTED_BOOK)));
        ItemStack water = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        assertEquals(16, StackSizes.resolve(water));
    }

    @Test
    void ordinaryItemsAreNotTouched() {
        assertEquals(-1, StackSizes.resolve(new ItemStack(Items.DIRT)));
        assertEquals(-1, StackSizes.resolve(new ItemStack(Items.ENDER_PEARL)));
    }

    @Test
    void blacklistForcesVanilla() {
        ModConfig.Cache.vanillaMode = "blacklist";
        ModConfig.Cache.vanillaList = new java.util.LinkedHashSet<>(java.util.Set.of("minecraft:diamond_pickaxe"));
        assertEquals(1, StackSizes.resolve(new ItemStack(Items.DIAMOND_PICKAXE)));
        assertEquals(16, StackSizes.resolve(new ItemStack(Items.IRON_SWORD)));
        resetLists();
    }

    @Test
    void whitelistOnlyAllowsListed() {
        ModConfig.Cache.vanillaMode = "whitelist";
        ModConfig.Cache.vanillaList = new java.util.LinkedHashSet<>(java.util.Set.of("minecraft:iron_sword"));
        assertEquals(1, StackSizes.resolve(new ItemStack(Items.DIAMOND_PICKAXE)));
        assertEquals(16, StackSizes.resolve(new ItemStack(Items.IRON_SWORD)));
        resetLists();
    }

    @Test
    void extraListOverridesEverything() {
        ModConfig.Cache.extraFixed.put("minecraft:dirt", 64);
        ModConfig.Cache.extraMultiplier.put("minecraft:ender_pearl", 4);
        assertEquals(64, StackSizes.resolve(new ItemStack(Items.DIRT)));
        assertEquals(64, StackSizes.resolve(new ItemStack(Items.ENDER_PEARL)));
        ModConfig.Cache.extraFixed.remove("minecraft:dirt");
        ModConfig.Cache.extraMultiplier.remove("minecraft:ender_pearl");
    }

    @Test
    void itemLevelResolverMatchesStackLevel() {
        assertEquals(16, StackSizes.resolveItem(Items.DIAMOND_PICKAXE));
        assertEquals(16, StackSizes.resolveItem(Items.IRON_SWORD));
        assertEquals(16, StackSizes.resolveItem(Items.ENCHANTED_BOOK));
        assertEquals(1, StackSizes.resolveItem(Items.TRIDENT));
        assertEquals(1, StackSizes.resolveItem(Items.CROSSBOW));
        assertEquals(-1, StackSizes.resolveItem(Items.DIRT));
        assertEquals(-1, StackSizes.resolveItem(Items.DIAMOND_CHESTPLATE));
    }

    private static void resetLists() {
        ModConfig.Cache.vanillaMode = "off";
        ModConfig.Cache.vanillaList = new java.util.LinkedHashSet<>();
    }
}
