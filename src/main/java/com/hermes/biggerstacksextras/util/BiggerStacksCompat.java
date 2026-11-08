package com.hermes.biggerstacksextras.util;

import java.lang.reflect.Field;

/**
 * 和 Bigger Stacks 的联动（反射，避免编译期依赖它的 jar）。
 *
 * 我们通过 IMC 规则把它的“规则上限”抬上去；这里再把它的
 * maxRegisteredItemStackSize 也抬一下，让漏斗/创造模式物品栏/掉落物合并
 * 的上限跟着放大，同时避免它每次登录都提示“未配置”。
 */
public final class BiggerStacksCompat {

    private static Field maxField;

    private BiggerStacksCompat() {
    }

    public static void ensureGlobalMax(int value) {
        try {
            if (maxField == null) {
                Class<?> clazz = Class.forName("portb.biggerstacks.config.StackSizeRules");
                maxField = clazz.getField("maxRegisteredItemStackSize");
            }
            int current = maxField.getInt(null);
            if (value > current) {
                maxField.setInt(null, value);
            }
        } catch (Throwable ignored) {
            // Bigger Stacks 不在或者版本不同：忽略，IMC 规则仍然生效。
        }
    }
}
