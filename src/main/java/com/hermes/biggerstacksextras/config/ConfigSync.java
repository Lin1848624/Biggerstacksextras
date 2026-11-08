package com.hermes.biggerstacksextras.config;

import com.hermes.biggerstacksextras.config.ModConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import com.hermes.biggerstacksextras.util.BiggerStacksCompat;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 服务器 → 客户端的配置同步。
 *
 * 服务端是权威；客户端收到后只改内存里的缓存，不写客户端的 TOML 文件。
 */
public final class ConfigSync {

    private ConfigSync() {
    }

    public static CompoundTag snapshot() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("books", ModConfig.Cache.bookSize);
        tag.putInt("potions", ModConfig.Cache.potionSize);
        tag.putInt("blood", ModConfig.Cache.bloodSize);
        tag.putInt("tools", ModConfig.Cache.toolSize);
        tag.putString("ammo_mode", ModConfig.Cache.ammoMode);
        tag.putInt("ammo_multiplier", ModConfig.Cache.ammoMultiplier);
        tag.putInt("ammo_fixed", ModConfig.Cache.ammoFixed);
        tag.putInt("ammo_fallback", ModConfig.Cache.ammoFallback);
        tag.putString("vanilla_mode", ModConfig.Cache.vanillaMode);
        tag.putString("gtm_mode", ModConfig.Cache.gtmMode);
        tag.putString("ammo_list_mode", ModConfig.Cache.ammoListMode);
        tag.put("vanilla_list", toList(ModConfig.Cache.vanillaList));
        tag.put("gtm_list", toList(ModConfig.Cache.gtmList));
        tag.put("ammo_list", toList(ModConfig.Cache.ammoList));
        tag.put("ammo_overrides", toList(ModConfig.Cache.ammoOverrides));
        tag.put("extra_fixed", toList(ModConfig.Cache.extraFixed));
        tag.put("extra_multiplier", toList(ModConfig.Cache.extraMultiplier));
        return tag;
    }

    public static void apply(CompoundTag tag) {
        if (tag.contains("books")) ModConfig.Cache.bookSize = clamp(tag.getInt("books"));
        if (tag.contains("potions")) ModConfig.Cache.potionSize = clamp(tag.getInt("potions"));
        if (tag.contains("blood")) ModConfig.Cache.bloodSize = clamp(tag.getInt("blood"));
        if (tag.contains("tools")) ModConfig.Cache.toolSize = clamp(tag.getInt("tools"));
        if (tag.contains("ammo_mode")) ModConfig.Cache.ammoMode = tag.getString("ammo_mode");
        if (tag.contains("ammo_multiplier")) ModConfig.Cache.ammoMultiplier = Math.max(1, tag.getInt("ammo_multiplier"));
        if (tag.contains("ammo_fixed")) ModConfig.Cache.ammoFixed = clamp(tag.getInt("ammo_fixed"));
        if (tag.contains("ammo_fallback")) ModConfig.Cache.ammoFallback = Math.max(1, tag.getInt("ammo_fallback"));
        if (tag.contains("vanilla_mode")) ModConfig.Cache.vanillaMode = tag.getString("vanilla_mode");
        if (tag.contains("gtm_mode")) ModConfig.Cache.gtmMode = tag.getString("gtm_mode");
        if (tag.contains("ammo_list_mode")) ModConfig.Cache.ammoListMode = tag.getString("ammo_list_mode");
        if (tag.contains("vanilla_list")) ModConfig.Cache.vanillaList = fromList(tag.getList("vanilla_list", Tag.TAG_STRING));
        if (tag.contains("gtm_list")) ModConfig.Cache.gtmList = fromList(tag.getList("gtm_list", Tag.TAG_STRING));
        if (tag.contains("ammo_list")) ModConfig.Cache.ammoList = fromList(tag.getList("ammo_list", Tag.TAG_STRING));
        if (tag.contains("ammo_overrides")) ModConfig.Cache.ammoOverrides = fromMap(tag.getList("ammo_overrides", Tag.TAG_STRING));
        if (tag.contains("extra_fixed")) ModConfig.Cache.extraFixed = fromMap(tag.getList("extra_fixed", Tag.TAG_STRING));
        if (tag.contains("extra_multiplier")) ModConfig.Cache.extraMultiplier = fromMap(tag.getList("extra_multiplier", Tag.TAG_STRING));

        BiggerStacksCompat.ensureGlobalMax(ModConfig.ruleMax());
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(1_000_000, value));
    }

    private static ListTag toList(Set<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf(value));
        }
        return list;
    }

    private static ListTag toList(Map<String, Integer> values) {
        ListTag list = new ListTag();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            list.add(StringTag.valueOf(entry.getKey() + "=" + entry.getValue()));
        }
        return list;
    }

    private static Set<String> fromList(ListTag list) {
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.getString(i));
        }
        return result;
    }

    private static Map<String, Integer> fromMap(ListTag list) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            String raw = list.getString(i);
            int idx = raw.lastIndexOf('=');
            if (idx <= 0) continue;
            try {
                result.put(raw.substring(0, idx), Integer.parseInt(raw.substring(idx + 1)));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }
}
