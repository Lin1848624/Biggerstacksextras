package com.hermes.biggerstacksextras.util;

import net.minecraft.world.entity.Entity;

import java.lang.reflect.Method;

/**
 * Butchery 的兼容：同样只用反射，没装时是空操作。
 *
 * 5.3.1 的感染血液瓶在 finishUsingItem 里调错了方法（调成了普通血液瓶的结算），
 * 结果喝感染瓶会额外扣一个普通血瓶、还不触发它自己的效果。
 * 这里改成调用它本来写好的感染瓶结算方法。
 */
public final class ButcheryCompat {

    private static final Method INFECTED_EFFECT;

    static {
        Method method = null;
        try {
            Class<?> clazz = Class.forName("net.mcreator.butchery.procedures.BottleofinfectedbloodPlayerFinishesUsingItemProcedure");
            method = clazz.getMethod("execute", Entity.class);
        } catch (Throwable ignored) {
            method = null;
        }
        INFECTED_EFFECT = method;
    }

    private ButcheryCompat() {
    }

    public static void applyInfectedEffect(Entity entity) {
        if (INFECTED_EFFECT == null || entity == null) {
            return;
        }
        try {
            INFECTED_EFFECT.invoke(null, entity);
        } catch (Throwable ignored) {
        }
    }
}
