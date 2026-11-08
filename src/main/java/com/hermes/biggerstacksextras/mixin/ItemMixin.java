package com.hermes.biggerstacksextras.mixin;

import com.hermes.biggerstacksextras.util.StackSizes;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 第二道保险。
 *
 * 有些模组（例如 Tweakerge）会在 ItemStack.getMaxStackSize 的 HEAD 直接 cancel，
 * 返回“物品自己的上限”。它们最终调用的就是 Item.getMaxStackSize()（无参）。
 * 我们在这里也拦一层，保证即使 ItemStack 那一层被人抢了先，返回的仍是我们的数值。
 *
 * 拿不到 ItemStack，所以这里只按物品类型算；TACZ 的弹种和 *倍数 的精确值
 * 仍然由 ItemStack 那一层负责。
 */
@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void bse$overrideItemMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        int value = StackSizes.resolveItem((Item) (Object) this);
        if (value > 0) {
            cir.setReturnValue(value);
            cir.cancel();
        }
    }
}
