package com.hermes.biggerstacksextras.event;

import com.hermes.biggerstacksextras.BiggerStacksExtras;
import com.hermes.biggerstacksextras.config.ModConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/** 配置加载/重载：把 TOML 的值读进 mixin 用的静态缓存。 */
@Mod.EventBusSubscriber(modid = BiggerStacksExtras.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModConfigEvents {

    private ModConfigEvents() {
    }

    @SubscribeEvent
    public static void onLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            ModConfig.attach(event.getConfig());
        }
    }

    @SubscribeEvent
    public static void onReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ModConfig.SPEC) {
            ModConfig.attach(event.getConfig());
        }
    }
}
