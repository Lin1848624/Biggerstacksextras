package com.hermes.biggerstacksextras.util;

import com.hermes.biggerstacksextras.config.ModConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/**
 * 所有“这个物品最多堆多少”的判断都集中在这里。
 *
 * 返回值 > 0 表示“由我们决定”；返回 -1 表示“不归我们管”，
 * 交回给原版 / Bigger Stacks 的规则。
 */
public final class StackSizes {

    public static final String TACZ_AMMO = "tacz:ammo";
    public static final String AMMO_ID_TAG = "AmmoId";

    private static final Set<String> POTIONS = Set.of(
            "minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion");

    private static final Set<String> BUTCHERY_BOTTLES = Set.of(
            "butchery:bottle_of_blood",
            "butchery:bottle_of_infected_blood",
            "butchery:bottle_of_sulfuric_acid");

    /**
     * 这几种原版武器不参与工具堆叠：
     * - 三叉戟投掷时会把整叠复制进弹射物实体，捡回来会复制物品；
     * - 十字弓的“已装填”状态记在整叠上，整叠共享一次装填。
     */
    private static final Set<String> DEFAULT_EXCLUDED = Set.of(
            "minecraft:trident", "minecraft:crossbow");

    private static final ThreadLocal<Boolean> RESOLVING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private StackSizes() {
    }

    public static int resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        // 防止别的模组的 getMaxStackSize(ItemStack) 反过来调 ItemStack.getMaxStackSize 造成递归。
        if (RESOLVING.get()) {
            return -1;
        }
        RESOLVING.set(Boolean.TRUE);
        try {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (key == null) {
                return -1;
            }
            String id = key.toString();

            // 这几种先拦掉：它们会把整叠复制进弹射物/共享装填状态，不能开放堆叠。
            if (DEFAULT_EXCLUDED.contains(id)) {
                return 1;
            }

            Integer extra = extraValue(id, stack);
            if (extra != null) {
                return extra;
            }

            if (BUTCHERY_BOTTLES.contains(id)) {
                return ModConfig.Cache.bloodSize;
            }
            if (POTIONS.contains(id)) {
                return ModConfig.Cache.potionSize;
            }
            if ("minecraft:enchanted_book".equals(id)) {
                return ModConfig.Cache.bookSize;
            }
            if (TACZ_AMMO.equals(id)) {
                return ammoSize(stack);
            }
            if (isHandSlot(stack)) {
                Item item = stack.getItem();
                if ("minecraft".equals(key.getNamespace()) && stack.isDamageableItem()) {
                    return toolAllowed(id) ? ModConfig.Cache.toolSize : 1;
                }
                if ("gtceu".equals(key.getNamespace()) && GtCompat.isGtTool(item)) {
                    // 电动工具用一格电量，整叠会共用/复制电量，强制保持 1。
                    if (GtCompat.isElectric(item)) {
                        return 1;
                    }
                    return toolAllowed(id) ? ModConfig.Cache.toolSize : 1;
                }
            }
            return -1;
        } finally {
            RESOLVING.set(Boolean.FALSE);
        }
    }

    /** 只按“物品类型”算上限（拿不到 ItemStack 时用；不处理 TACZ 弹种和 *倍数 的精确值）。 */
    public static int resolveItem(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            return -1;
        }
        String id = key.toString();
        if (DEFAULT_EXCLUDED.contains(id)) {
            return 1;
        }
        Integer extra = ModConfig.Cache.extraFixed.get(id);
        if (extra != null) {
            return extra;
        }
        Integer multiplier = ModConfig.Cache.extraMultiplier.get(id);
        if (multiplier != null) {
            return Math.max(1, Math.min(1_000_000, 64 * multiplier));
        }
        if (BUTCHERY_BOTTLES.contains(id)) {
            return ModConfig.Cache.bloodSize;
        }
        if (POTIONS.contains(id)) {
            return ModConfig.Cache.potionSize;
        }
        if ("minecraft:enchanted_book".equals(id)) {
            return ModConfig.Cache.bookSize;
        }
        if (TACZ_AMMO.equals(id)) {
            // 拿不到弹种，只能用统一值/缺省值兜底；正常路径是 ItemStack 版本。
            if ("fixed".equals(ModConfig.Cache.ammoMode)) {
                return ModConfig.Cache.ammoFixed;
            }
            return (int) Math.max(1L, Math.min(1_000_000L,
                    (long) ModConfig.Cache.ammoFallback * ModConfig.Cache.ammoMultiplier));
        }
        if (isVanillaToolItem(key, item)) {
            return toolAllowed(id) ? ModConfig.Cache.toolSize : 1;
        }
        if (isGtToolItem(key, item)) {
            return GtCompat.isElectric(item) ? 1 : (toolAllowed(id) ? ModConfig.Cache.toolSize : 1);
        }
        return -1;
    }

    private static boolean isVanillaToolItem(ResourceLocation key, Item item) {
        if (!"minecraft".equals(key.getNamespace()) || item.getMaxDamage() <= 0) {
            return false;
        }
        return !(item instanceof net.minecraft.world.item.ArmorItem)
                && !(item instanceof net.minecraft.world.item.ElytraItem);
    }

    private static boolean isGtToolItem(ResourceLocation key, Item item) {
        return "gtceu".equals(key.getNamespace()) && GtCompat.isGtTool(item);
    }

    private static boolean isHandSlot(ItemStack stack) {
        EquipmentSlot slot = Mob.getEquipmentSlotForItem(stack);
        return slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
    }

    public static String idOf(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? null : key.toString();
    }

    /** 是不是“拿在手上的耐久工具/武器”，并且不在排除范围（盔甲、鞘翅、GT 电动工具）。 */
    public static boolean isStackableTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!isHandSlot(stack)) {
            return false;
        }
        Item item = stack.getItem();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            return false;
        }
        String namespace = key.getNamespace();
        if ("minecraft".equals(namespace)) {
            return !DEFAULT_EXCLUDED.contains(key.toString()) && stack.isDamageableItem();
        }
        if ("gtceu".equals(namespace)) {
            return GtCompat.isGtTool(item) && !GtCompat.isElectric(item);
        }
        return false;
    }

    public static boolean isStackedTool(ItemStack stack) {
        return stack.getCount() > 1 && isStackableTool(stack);
    }

    private static boolean toolAllowed(String id) {
        ResourceLocation key = ResourceLocation.tryParse(id);
        String namespace = key == null ? "" : key.getNamespace();
        if ("gtceu".equals(namespace)) {
            return allowed(ModConfig.Cache.gtmMode, ModConfig.Cache.gtmList, id);
        }
        return allowed(ModConfig.Cache.vanillaMode, ModConfig.Cache.vanillaList, id);
    }

    private static boolean allowed(String mode, Set<String> list, String id) {
        if ("blacklist".equals(mode)) {
            return !list.contains(id);
        }
        if ("whitelist".equals(mode)) {
            return list.contains(id);
        }
        return true;
    }

    private static Integer extraValue(String id, ItemStack stack) {
        Integer fixed = ModConfig.Cache.extraFixed.get(id);
        if (fixed != null) {
            return fixed;
        }
        Integer multiplier = ModConfig.Cache.extraMultiplier.get(id);
        if (multiplier != null) {
            int base = stack.getItem().getMaxStackSize();
            long value = (long) Math.max(1, base) * multiplier;
            return (int) Math.max(1, Math.min(1_000_000, value));
        }
        return null;
    }

    private static int ammoSize(ItemStack stack) {
        String ammoId = ammoId(stack);
        if (ammoId.isEmpty()) {
            return -1; // 没有 AmmoId 的 tacz:ammo 是无效物品，交回原版
        }
        if (!allowed(ModConfig.Cache.ammoListMode, ModConfig.Cache.ammoList, ammoId)) {
            return Math.max(1, baseAmmo(stack));
        }
        Integer override = ModConfig.Cache.ammoOverrides.get(ammoId);
        if (override != null) {
            return override;
        }
        if ("fixed".equals(ModConfig.Cache.ammoMode)) {
            return ModConfig.Cache.ammoFixed;
        }
        int base = baseAmmo(stack);
        if (base <= 1) {
            base = ModConfig.Cache.ammoFallback;
        }
        long value = (long) base * ModConfig.Cache.ammoMultiplier;
        return (int) Math.max(1, Math.min(1_000_000, value));
    }

    private static int baseAmmo(ItemStack stack) {
        // TACZ 的 AmmoItem 重写了这个重载，返回“这种弹自己的原上限”（9mm 60、.338 30、火箭 6）。
        int base = stack.getItem().getMaxStackSize(stack);
        return base > 0 ? base : 1;
    }

    public static String ammoId(ItemStack stack) {
        if (stack.getTag() == null) {
            return "";
        }
        return stack.getTag().getString(AMMO_ID_TAG);
    }

    public static boolean hasTag(ItemStack stack, String tag) {
        return stack.getTag() != null && stack.getTag().getBoolean(tag);
    }
}
