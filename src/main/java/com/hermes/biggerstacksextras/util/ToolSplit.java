package com.hermes.biggerstacksextras.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * “用一把、分一把”。
 *
 * 工具堆叠时，真正的耐久只应该记在正在用的那一把上：
 * 第一次要用到耐久时，把 1 把留在手上，其余的按原样退回背包。
 * 这样被用过的和没被用过的数据不同，自然就并不回去了。
 */
public final class ToolSplit {

    private ToolSplit() {
    }

    /** 在扣耐久之前调用：扣完之后这一叠只剩 1 把，其余回到背包。 */
    public static void splitOne(ItemStack stack, LivingEntity user, int damage) {
        if (stack.getCount() <= 1 || damage <= 0) {
            return;
        }
        if (!(user instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide || player.getAbilities().instabuild) {
            return;
        }
        if (!StackSizes.isStackableTool(stack)) {
            return;
        }
        if (GtCompat.isElectric(stack.getItem())) {
            return;
        }
        // GT 的“不可破坏”工具不消耗耐久，不要拆。
        if (StackSizes.hasTag(stack, "Unbreakable")) {
            return;
        }

        ItemStack rest = stack.copy();
        rest.setCount(stack.getCount() - 1);
        stack.setCount(1);
        if (!player.getInventory().add(rest)) {
            player.drop(rest, false);
        }
    }
}
