package com.hermes.biggerstacksextras.util;

import net.minecraft.world.item.Item;

import java.lang.reflect.Method;

/**
 * GTCEu Modern 的兼容：只用反射判断，不在编译期依赖它。
 * 没装 GT 时这里全部返回 false。
 */
public final class GtCompat {

    private static final Class<?> TOOL_CLASS;
    private static final Method IS_ELECTRIC;

    static {
        Class<?> toolClass = null;
        Method isElectric = null;
        try {
            toolClass = Class.forName("com.gregtechceu.gtceu.api.item.IGTTool");
            isElectric = toolClass.getMethod("isElectric");
        } catch (Throwable ignored) {
            toolClass = null;
            isElectric = null;
        }
        TOOL_CLASS = toolClass;
        IS_ELECTRIC = isElectric;
    }

    private GtCompat() {
    }

    public static boolean isGtTool(Item item) {
        return TOOL_CLASS != null && TOOL_CLASS.isInstance(item);
    }

    public static boolean isElectric(Item item) {
        if (TOOL_CLASS == null || IS_ELECTRIC == null || !TOOL_CLASS.isInstance(item)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(IS_ELECTRIC.invoke(item));
        } catch (Throwable ignored) {
            return false;
        }
    }
}
