package com.hermes.biggerstacksextras.mixin;

import com.hermes.biggerstacksextras.util.HurtTracker;
import com.hermes.biggerstacksextras.util.StackSizes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * 三件事：
 * 1. 在 getMaxStackSize 最前面返回我们算的值（这样能绕开 Bigger Stacks 按物品缓存的限制，TACZ 子弹才可能按弹种区分）；
 * 2. 允许“同耐久的工具”互相并堆（原版只要有耐久损伤就不让并）；
 * 3. 工具真正掉耐久时，把 1 把留在手上、其余原样退回背包。
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void bse$overrideMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        int value = StackSizes.resolve((ItemStack) (Object) this);
        if (value > 0) {
            cir.setReturnValue(value);
            cir.cancel();
        }
    }

    @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
    private void bse$allowStackedTools(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getMaxStackSize() > 1 && StackSizes.isStackableTool(self)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "hurtAndBreak", at = @At("HEAD"))
    private void bse$recordBeforeHurt(int amount, LivingEntity entity, Consumer<LivingEntity> onBreak, CallbackInfo ci) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getCount() <= 1) {
            return;
        }
        if (!(entity instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide || player.getAbilities().instabuild) {
            return;
        }
        if (!self.isDamageableItem() || !StackSizes.isStackableTool(self)) {
            return;
        }
        HurtTracker.begin(self.getCount(), self.getDamageValue());
    }

    @Inject(method = "hurtAndBreak", at = @At("RETURN"))
    private void bse$splitAfterHurt(int amount, LivingEntity entity, Consumer<LivingEntity> onBreak, CallbackInfo ci) {
        int[] state = HurtTracker.take();
        if (state == null || !(entity instanceof Player player)) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;
        int countBefore = state[0];
        int damageBefore = state[1];

        if (self.getCount() < countBefore) {
            // 这一把刚好损坏：原版把整叠的耐久清零了，把剩下那几把恢复成原来的耐久。
            self.setDamageValue(damageBefore);
            return;
        }
        if (self.getDamageValue() <= damageBefore || self.getCount() <= 1) {
            // 没真的掉耐久（耐久保护生效），或者已经不是整叠了，不动。
            return;
        }

        ItemStack rest = self.copy();
        rest.setCount(self.getCount() - 1);
        rest.setDamageValue(damageBefore);
        self.setCount(1);
        if (!player.getInventory().add(rest)) {
            player.drop(rest, false);
        }
    }
}
