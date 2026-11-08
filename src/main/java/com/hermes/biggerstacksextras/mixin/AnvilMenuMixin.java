package com.hermes.biggerstacksextras.mixin;

import com.hermes.biggerstacksextras.util.StackSizes;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 铁砧里只要有一边是“整叠工具”，就不出结果。
 *
 * 原因：原版修一件装备的材料消耗是按“这一叠”的耐久算的，
 * 一叠 5 把同耐久的镐子会被当成 1 把收钱，等于打 2 折。
 * 我们不让修，玩家拆开再修，价格就正常了。
 *
 * 只清空结果栏，不动费用数字（费用只是显示，拿不出东西）。
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void bse$blockStackedTools(CallbackInfo ci) {
        AnvilMenu self = (AnvilMenu) (Object) this;
        // 槽位顺序：0 = 左输入，1 = 右输入，2 = 结果。
        if (StackSizes.isStackedTool(self.getSlot(0).getItem()) || StackSizes.isStackedTool(self.getSlot(1).getItem())) {
            self.getSlot(2).set(ItemStack.EMPTY);
            ci.cancel();
        }
    }
}
