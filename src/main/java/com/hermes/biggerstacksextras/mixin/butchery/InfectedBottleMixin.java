package com.hermes.biggerstacksextras.mixin.butchery;

import com.hermes.biggerstacksextras.util.ButcheryCompat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Butchery 5.3.1 的感染血液瓶在 finishUsingItem 里调用了普通血液瓶的结算过程：
 * 会从背包再扣一个普通血瓶，而且感染瓶本来的效果没执行。
 *
 * 这里把那次调用换掉：执行感染瓶自己的过程，并补一个玻璃瓶。
 * 只在装了 butchery 时应用（ButcheryMixinPlugin 控制）。
 *
 * 方法名 m_5922_ 是 1.20.1 的 Item#finishUsingItem 混淆名；
 * 目标类是模组类，不能重映射，所以整个 mixin 用 remap = false。
 */
@Pseudo
@Mixin(targets = "net.mcreator.butchery.item.BottleofinfectedbloodItem", remap = false)
public abstract class InfectedBottleMixin {

    @Redirect(
            method = "m_5922_",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/mcreator/butchery/procedures/BottleofbloodPlayerFinishesUsingItemProcedure;execute(Lnet/minecraft/world/entity/Entity;)V"))
    private void bse$fixInfectedBottle(Entity entity) {
        ButcheryCompat.applyInfectedEffect(entity);
        if (entity instanceof Player player && !player.level().isClientSide) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        }
    }
}
