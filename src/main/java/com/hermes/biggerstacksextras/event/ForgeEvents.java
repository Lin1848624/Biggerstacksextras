package com.hermes.biggerstacksextras.event;

import com.hermes.biggerstacksextras.BiggerStacksExtras;
import com.hermes.biggerstacksextras.command.ModCommands;
import com.hermes.biggerstacksextras.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 普通 Forge 总线上的事件。 */
@Mod.EventBusSubscriber(modid = BiggerStacksExtras.MOD_ID)
public final class ForgeEvents {

    private ForgeEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            com.hermes.biggerstacksextras.BiggerStacksExtras.LOGGER.info(
                    "[bsextras] 发送配置给 {}：书={} 药水={} 工具={} 子弹={}x{}",
                    player.getName().getString(),
                    com.hermes.biggerstacksextras.config.ModConfig.Cache.bookSize,
                    com.hermes.biggerstacksextras.config.ModConfig.Cache.potionSize,
                    com.hermes.biggerstacksextras.config.ModConfig.Cache.toolSize,
                    com.hermes.biggerstacksextras.config.ModConfig.Cache.ammoMode,
                    com.hermes.biggerstacksextras.config.ModConfig.Cache.ammoMultiplier);
            NetworkHandler.syncTo(player);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
