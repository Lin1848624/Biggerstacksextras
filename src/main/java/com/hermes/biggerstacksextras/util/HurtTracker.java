package com.hermes.biggerstacksextras.util;

/**
 * 记录“扣耐久之前”的数量和耐久，供 ItemStackMixin 在方法返回时对比。
 * 用 ThreadLocal 避免每条 ItemStack 都多带几个字段。
 */
public final class HurtTracker {

    private static final ThreadLocal<int[]> STATE = ThreadLocal.withInitial(() -> new int[3]);

    private HurtTracker() {
    }

    public static void begin(int count, int damage) {
        int[] state = STATE.get();
        state[0] = count;
        state[1] = damage;
        state[2] = 1;
    }

    /** 返回 [countBefore, damageBefore]，没有记录时返回 null。 */
    public static int[] take() {
        int[] state = STATE.get();
        if (state[2] == 0) {
            return null;
        }
        state[2] = 0;
        return state;
    }
}
