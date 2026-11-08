package com.hermes.biggerstacksextras;

import com.hermes.biggerstacksextras.config.ModConfig;
import com.hermes.biggerstacksextras.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * 模组入口。
 *
 * 硬依赖只有 Bigger Stacks（超过 127 的堆叠存盘/联机要靠它）。
 * TACZ、Butchery、GTCEu 都是软兼容：装了才生效，没装不加载它们的类。
 */
@Mod(BiggerStacksExtras.MOD_ID)
public final class BiggerStacksExtras {

    public static final String MOD_ID = "biggerstacksextras";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BiggerStacksExtras() {
        LOGGER.info("[bsextras] 正在初始化（mixin 优先级 100）");
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                ModConfig.SPEC, "biggerstacksextras-common.toml");

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        // 给 Bigger Stacks 发一条永不匹配的规则，只把它的“全局最大堆叠”抬上去，
        // 让漏斗、创造模式物品栏、掉落物合并跟着放大。走官方 IMC 接口，不改它的文件。
        InterModComms.sendTo("biggerstacks", "add_rules", ModConfig::imcRuleXml);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
