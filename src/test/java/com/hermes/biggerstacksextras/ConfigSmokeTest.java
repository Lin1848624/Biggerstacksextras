package com.hermes.biggerstacksextras;

import com.hermes.biggerstacksextras.config.ConfigSync;
import com.hermes.biggerstacksextras.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 不启动游戏也能验证：配置定义能建起来，同步包能来回转换。 */
class ConfigSmokeTest {

    @Test
    void specBuilds() {
        assertNotNull(ModConfig.SPEC);
    }

    @Test
    void syncRoundTrip() {
        ModConfig.Cache.bookSize = 32;
        ModConfig.Cache.potionSize = 8;
        ModConfig.Cache.bloodSize = 24;
        ModConfig.Cache.toolSize = 12;
        ModConfig.Cache.ammoMode = "fixed";
        ModConfig.Cache.ammoFixed = 700;
        ModConfig.Cache.ammoMultiplier = 20;
        ModConfig.Cache.vanillaMode = "blacklist";
        ModConfig.Cache.vanillaList.clear();
        ModConfig.Cache.vanillaList.add("minecraft:wooden_pickaxe");

        CompoundTag tag = ConfigSync.snapshot();

        ModConfig.Cache.bookSize = 1;
        ModConfig.Cache.potionSize = 1;
        ModConfig.Cache.bloodSize = 1;
        ModConfig.Cache.toolSize = 1;
        ModConfig.Cache.ammoMode = "multiplier";
        ModConfig.Cache.ammoFixed = 1;
        ModConfig.Cache.ammoMultiplier = 1;
        ModConfig.Cache.vanillaMode = "off";
        ModConfig.Cache.vanillaList.clear();

        ConfigSync.apply(tag);

        assertEquals(32, ModConfig.Cache.bookSize);
        assertEquals(8, ModConfig.Cache.potionSize);
        assertEquals(24, ModConfig.Cache.bloodSize);
        assertEquals(12, ModConfig.Cache.toolSize);
        assertEquals("fixed", ModConfig.Cache.ammoMode);
        assertEquals(700, ModConfig.Cache.ammoFixed);
        assertEquals(20, ModConfig.Cache.ammoMultiplier);
        assertEquals("blacklist", ModConfig.Cache.vanillaMode);
        assertTrue(ModConfig.Cache.vanillaList.contains("minecraft:wooden_pickaxe"));
    }
}
