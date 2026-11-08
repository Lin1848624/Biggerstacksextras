package com.hermes.biggerstacksextras.config;

import com.hermes.biggerstacksextras.util.BiggerStacksCompat;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 配置定义 + 一份给 mixin 直接读的静态缓存。
 *
 * mixin 可能比配置加载更早被调用，所以 mixin 只读 ConfigCache 里的值；
 * 这些值在配置加载/重载时刷新，默认值 = 原版行为（1 = 不堆）。
 */
public final class ModConfig {

    public static final ForgeConfigSpec SPEC;

    // 数值
    public static final ForgeConfigSpec.IntValue BOOK_SIZE;
    public static final ForgeConfigSpec.IntValue POTION_SIZE;
    public static final ForgeConfigSpec.IntValue BLOOD_SIZE;
    public static final ForgeConfigSpec.IntValue TOOL_SIZE;

    public static final ForgeConfigSpec.ConfigValue<String> AMMO_MODE;
    public static final ForgeConfigSpec.IntValue AMMO_MULTIPLIER;
    public static final ForgeConfigSpec.IntValue AMMO_FIXED;
    public static final ForgeConfigSpec.IntValue AMMO_FALLBACK;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AMMO_OVERRIDES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_ENTRIES;

    public static final ForgeConfigSpec.ConfigValue<String> VANILLA_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VANILLA_LIST;
    public static final ForgeConfigSpec.ConfigValue<String> GTM_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> GTM_LIST;
    public static final ForgeConfigSpec.ConfigValue<String> AMMO_LIST_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AMMO_LIST;

    private static net.minecraftforge.fml.config.ModConfig configFile;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment(
                "Bigger Stacks Extras 配置。",
                "所有数值：1 = 保持原版（不堆叠），最大 1000000。",
                "改完文件需要重启游戏；游戏内用 /bsextras set 改可以立即生效。")
                .push("general");

        BOOK_SIZE = builder.comment("附魔书一叠多少个。默认 16。")
                .defineInRange("enchanted_book_size", 16, 1, 1_000_000);
        POTION_SIZE = builder.comment("药水（含水瓶、喷溅、滞留）一叠多少个。默认 16。")
                .defineInRange("potion_size", 16, 1, 1_000_000);
        BLOOD_SIZE = builder.comment("Butchery 的血液瓶 / 感染血液瓶一叠多少个。默认 16。没装 Butchery 时这一项不起作用。")
                .defineInRange("blood_bottle_size", 16, 1, 1_000_000);
        TOOL_SIZE = builder.comment("工具 / 武器一叠多少个。默认 16。只影响原版工具和 GTCEu 耐久工具，不含盔甲、鞘翅、电动工具。")
                .defineInRange("tool_size", 16, 1, 1_000_000);

        AMMO_MODE = builder.comment("TACZ 子弹的算法：multiplier = 每种子弹按自己的原上限乘倍数；fixed = 全部统一一个值。")
                .defineInList("ammo_mode", "multiplier", List.of("multiplier", "fixed"));
        AMMO_MULTIPLIER = builder.comment("ammo_mode=multiplier 时的倍数。默认 10：9mm 600、.338 300、火箭 60。")
                .defineInRange("ammo_multiplier", 10, 1, 10_000);
        AMMO_FIXED = builder.comment("ammo_mode=fixed 时所有子弹统一堆多少。默认 600。")
                .defineInRange("ammo_fixed", 600, 1, 1_000_000);
        AMMO_FALLBACK = builder.comment("枪包没写 stack_size 时，按这个值当作子弹原上限再乘倍数。默认 64。")
                .defineInRange("ammo_fallback_base", 64, 1, 10_000);

        AMMO_OVERRIDES = builder.comment(
                        "按弹种单独指定上限，写法 \"tacz:9mm=800\"。",
                        "这里的优先级最高，会盖过倍数/统一值。")
                .defineListAllowEmpty("ammo_overrides", List.of(), o -> o instanceof String);

        EXTRA_ENTRIES = builder.comment(
                        "额外物品清单，优先级最高。写法：",
                        "  \"minecraft:ender_pearl=64\"  固定 64",
                        "  \"some_mod:some_item=*4\"      按它自己的原上限乘 4",
                        "没装对应模组时写在这里也不会报错，只是找不到物品，自动忽略。")
                .defineListAllowEmpty("extra_entries", List.of(), o -> o instanceof String);

        builder.pop();

        builder.comment(
                "原版工具的黑白名单。",
                "list_mode：off = 全部按 tool_size 生效；blacklist = 名单里的不堆；whitelist = 只有名单里的堆。",
                "不在名单里的工具会强制回到原版上限（1），不会被 Bigger Stacks 的通用规则又堆起来。")
                .push("tools_vanilla");
        VANILLA_MODE = builder.defineInList("list_mode", "off", List.of("off", "blacklist", "whitelist"));
        VANILLA_LIST = builder.comment("物品 id 列表，例如 minecraft:diamond_pickaxe。")
                .defineListAllowEmpty("list", List.of(), o -> o instanceof String);
        builder.pop();

        builder.comment("GTCEu Modern（GTM）工具的黑白名单，规则同上。物品 id 例如 gtceu:lv_wrench。")
                .push("tools_gtm");
        GTM_MODE = builder.defineInList("list_mode", "off", List.of("off", "blacklist", "whitelist"));
        GTM_LIST = builder.comment("物品 id 列表。")
                .defineListAllowEmpty("list", List.of(), o -> o instanceof String);
        builder.pop();

        builder.comment("TACZ 子弹的黑白名单。注意：这里的名单写的是弹种 id（例如 tacz:9mm），不是物品 id。")
                .push("tacz_ammo");
        AMMO_LIST_MODE = builder.defineInList("list_mode", "off", List.of("off", "blacklist", "whitelist"));
        AMMO_LIST = builder.comment("弹种 id 列表，例如 tacz:9mm、tacz:556x45。")
                .defineListAllowEmpty("list", List.of(), o -> o instanceof String);
        builder.pop();

        SPEC = builder.build();
    }

    private ModConfig() {
    }

    public static void attach(net.minecraftforge.fml.config.ModConfig config) {
        configFile = config;
        refresh();
        BiggerStacksCompat.ensureGlobalMax(ruleMax());
    }

    public static void save() {
        if (configFile != null) {
            configFile.save();
        }
    }

    /** 把 TOML 里的值读进静态缓存。配置没加载好时保持上一份，不要把缓存冲成 1/0。 */
    public static void refresh() {
        if (!SPEC.isLoaded()) {
            return;
        }
        Cache.bookSize = readInt(BOOK_SIZE, Cache.bookSize);
        Cache.potionSize = readInt(POTION_SIZE, Cache.potionSize);
        Cache.bloodSize = readInt(BLOOD_SIZE, Cache.bloodSize);
        Cache.toolSize = readInt(TOOL_SIZE, Cache.toolSize);
        Cache.ammoMode = readString(AMMO_MODE, Cache.ammoMode);
        Cache.ammoMultiplier = readInt(AMMO_MULTIPLIER, Cache.ammoMultiplier);
        Cache.ammoFixed = readInt(AMMO_FIXED, Cache.ammoFixed);
        Cache.ammoFallback = readInt(AMMO_FALLBACK, Cache.ammoFallback);

        Cache.vanillaMode = readString(VANILLA_MODE, Cache.vanillaMode);
        Cache.gtmMode = readString(GTM_MODE, Cache.gtmMode);
        Cache.ammoListMode = readString(AMMO_LIST_MODE, Cache.ammoListMode);

        Cache.vanillaList = readSet(VANILLA_LIST, Cache.vanillaList);
        Cache.gtmList = readSet(GTM_LIST, Cache.gtmList);
        Cache.ammoList = readSet(AMMO_LIST, Cache.ammoList);

        Cache.ammoOverrides = readFixedList(AMMO_OVERRIDES, Cache.ammoOverrides);
        Map<String, Integer> fixed = new LinkedHashMap<>();
        Map<String, Integer> multiplier = new LinkedHashMap<>();
        try {
            parseExtra(EXTRA_ENTRIES.get(), fixed, multiplier);
            Cache.extraFixed = fixed;
            Cache.extraMultiplier = multiplier;
        } catch (Throwable ignored) {
        }

        com.hermes.biggerstacksextras.BiggerStacksExtras.LOGGER.info(
                "[bsextras] 配置: 书={} 药水={} 血瓶={} 工具={} 子弹={}x{} (fixed={}) 名单={}/{}/{}",
                Cache.bookSize, Cache.potionSize, Cache.bloodSize, Cache.toolSize,
                Cache.ammoMode, Cache.ammoMultiplier, Cache.ammoFixed,
                Cache.vanillaMode, Cache.gtmMode, Cache.ammoListMode);
    }

    private static int readInt(ForgeConfigSpec.IntValue value, int fallback) {
        try {
            int v = value.get();
            return v >= 1 ? Math.min(v, 1_000_000) : fallback;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static String readString(ForgeConfigSpec.ConfigValue<String> value, String fallback) {
        try {
            String v = value.get();
            return v == null || v.isEmpty() ? fallback : v;
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static Set<String> readSet(ForgeConfigSpec.ConfigValue<List<? extends String>> value, Set<String> fallback) {
        try {
            return new LinkedHashSet<>(value.get());
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static Map<String, Integer> readFixedList(ForgeConfigSpec.ConfigValue<List<? extends String>> value,
                                                      Map<String, Integer> fallback) {
        try {
            return parseFixedList(value.get());
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(1_000_000, value));
    }

    private static Map<String, Integer> parseFixedList(List<? extends String> entries) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (String raw : entries) {
            if (raw == null) continue;
            int idx = raw.lastIndexOf('=');
            if (idx <= 0) continue;
            try {
                map.put(raw.substring(0, idx).trim(), clamp(Integer.parseInt(raw.substring(idx + 1).trim())));
            } catch (NumberFormatException ignored) {
            }
        }
        return map;
    }

    private static void parseExtra(List<? extends String> entries, Map<String, Integer> fixed, Map<String, Integer> multiplier) {
        for (String raw : entries) {
            if (raw == null) continue;
            int idx = raw.lastIndexOf('=');
            if (idx <= 0) continue;
            String id = raw.substring(0, idx).trim();
            String value = raw.substring(idx + 1).trim();
            try {
                if (value.startsWith("*")) {
                    multiplier.put(id, Math.max(1, Integer.parseInt(value.substring(1).trim())));
                } else {
                    fixed.put(id, clamp(Integer.parseInt(value)));
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    /** 给 Bigger Stacks 的 IMC 规则用：我们可能用到的最大上限。 */
    public static int ruleMax() {
        int max = Math.max(Math.max(Cache.bookSize, Cache.potionSize), Math.max(Cache.bloodSize, Cache.toolSize));
        max = Math.max(max, Cache.ammoFixed);
        max = Math.max(max, Cache.ammoFallback * Cache.ammoMultiplier);
        for (int v : Cache.ammoOverrides.values()) max = Math.max(max, v);
        for (int v : Cache.extraFixed.values()) max = Math.max(max, v);
        for (int v : Cache.extraMultiplier.values()) max = Math.max(max, 64 * v);
        return Math.max(64, Math.min(1_000_000, max));
    }

    /**
     * 发一条永远不会匹配任何物品的规则，只用来把 Bigger Stacks 的“全局最大堆叠”
     * 抬到我们的数值，让漏斗、创造模式物品栏、掉落物合并的上限跟着放大。
     */
    public static String imcRuleXml() {
        try {
            refresh();
        } catch (Throwable ignored) {
            // 配置还没加载：用缓存里的默认值，规则也能用。
        }
        return "<rule stacksize=\"" + ruleMax() + "\">"
                + "<condition>id = biggerstacksextras:__never__</condition>"
                + "</rule>";
    }

    /** mixin 读的缓存。 */
    public static final class Cache {
        public static volatile int bookSize = 16;
        public static volatile int potionSize = 16;
        public static volatile int bloodSize = 16;
        public static volatile int toolSize = 16;

        public static volatile String ammoMode = "multiplier";
        public static volatile int ammoMultiplier = 10;
        public static volatile int ammoFixed = 600;
        public static volatile int ammoFallback = 64;

        public static volatile String vanillaMode = "off";
        public static volatile String gtmMode = "off";
        public static volatile String ammoListMode = "off";

        public static volatile Set<String> vanillaList = new LinkedHashSet<>();
        public static volatile Set<String> gtmList = new LinkedHashSet<>();
        public static volatile Set<String> ammoList = new LinkedHashSet<>();

        public static volatile Map<String, Integer> ammoOverrides = new LinkedHashMap<>();
        public static volatile Map<String, Integer> extraFixed = new LinkedHashMap<>();
        public static volatile Map<String, Integer> extraMultiplier = new LinkedHashMap<>();

        private Cache() {
        }

        public static List<String> copyOf(Set<String> set) {
            return new ArrayList<>(set);
        }
    }
}
