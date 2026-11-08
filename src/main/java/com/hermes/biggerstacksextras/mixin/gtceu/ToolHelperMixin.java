package com.hermes.biggerstacksextras.mixin.gtceu;

import com.hermes.biggerstacksextras.util.ToolSplit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GTCEu 的工具不经过原版 hurtAndBreak，而是走 ToolHelper.damageItem。
 * 在这里挂一个同样的“用一把、分一把”，保证 GT 工具堆叠时耐久只记在手上那把。
 *
 * 这个 mixin 只在装了 gtceu 时应用（GtMixinPlugin 控制）。
 */
@Pseudo
@Mixin(targets = "com.gregtechceu.gtceu.api.item.tool.ToolHelper", remap = false)
public abstract class ToolHelperMixin {

    @Inject(
            method = "damageItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;I)V",
            at = @At("HEAD"))
    private static void bse$splitGtTool(ItemStack stack, LivingEntity user, int damage, CallbackInfo ci) {
        ToolSplit.splitOne(stack, user, damage);
    }
}
