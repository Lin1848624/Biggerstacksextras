package com.hermes.biggerstacksextras.command;

import com.hermes.biggerstacksextras.config.ModConfig;
import com.hermes.biggerstacksextras.network.NetworkHandler;
import com.hermes.biggerstacksextras.util.BiggerStacksCompat;
import com.hermes.biggerstacksextras.util.StackSizes;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * /bsextras 命令树。
 *
 * 所有 set / list 操作都会：改内存 → 写 TOML → 广播给客户端 → 立即生效。
 */
public final class ModCommands {

    private static final SuggestionProvider<CommandSourceStack> MODES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(List.of("off", "blacklist", "whitelist"), builder);

    private static final SuggestionProvider<CommandSourceStack> AMMO_MODES =
            (ctx, builder) -> SharedSuggestionProvider.suggest(List.of("multiplier", "fixed"), builder);

    private ModCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bsextras")
                .requires(src -> !src.getServer().isDedicatedServer() || src.hasPermission(2))
                .then(Commands.literal("get").executes(ModCommands::get))
                .then(Commands.literal("help")
                        .executes(ModCommands::help)
                        .then(Commands.argument("key", StringArgumentType.word())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(helpKeys(), b))
                                .executes(ModCommands::helpKey)))
                .then(Commands.literal("set")
                        .then(Commands.literal("books").then(Commands.argument("value", IntegerArgumentType.integer(1, 1_000_000))
                                .executes(c -> setInt(c, ModConfig.BOOK_SIZE, "附魔书上限"))))
                        .then(Commands.literal("potions").then(Commands.argument("value", IntegerArgumentType.integer(1, 1_000_000))
                                .executes(c -> setInt(c, ModConfig.POTION_SIZE, "药水上限"))))
                        .then(Commands.literal("blood").then(Commands.argument("value", IntegerArgumentType.integer(1, 1_000_000))
                                .executes(c -> setInt(c, ModConfig.BLOOD_SIZE, "血液瓶上限"))))
                        .then(Commands.literal("tools").then(Commands.argument("value", IntegerArgumentType.integer(1, 1_000_000))
                                .executes(c -> setInt(c, ModConfig.TOOL_SIZE, "工具上限"))))
                        .then(Commands.literal("ammo_mode").then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(AMMO_MODES)
                                .executes(ModCommands::setAmmoMode)))
                        .then(Commands.literal("ammo_multiplier").then(Commands.argument("value", IntegerArgumentType.integer(1, 10_000))
                                .executes(c -> setInt(c, ModConfig.AMMO_MULTIPLIER, "子弹倍数"))))
                        .then(Commands.literal("ammo_fixed").then(Commands.argument("value", IntegerArgumentType.integer(1, 1_000_000))
                                .executes(c -> setInt(c, ModConfig.AMMO_FIXED, "子弹统一上限"))))
                        .then(Commands.literal("ammo")
                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 1_000_000))
                                                .executes(ModCommands::setAmmoOverride))))
                        .then(Commands.literal("extra")
                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .executes(ModCommands::setExtra)))))
                .then(Commands.literal("list")
                        .then(listNode("tools", ListCategory.VANILLA))
                        .then(listNode("gtm", ListCategory.GTM))
                        .then(listNode("ammo", ListCategory.AMMO))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> listNode(String name, ListCategory category) {
        return Commands.literal(name)
                .then(Commands.literal("mode")
                        .then(Commands.argument("mode", StringArgumentType.word())
                                .suggests(MODES)
                                .executes(ctx -> setMode(ctx, category))))
                .then(Commands.literal("add")
                        .executes(ctx -> addHeld(ctx, category))
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .executes(ctx -> addId(ctx, category))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .executes(ctx -> removeId(ctx, category))))
                .then(Commands.literal("show").executes(ctx -> show(ctx, category)));
    }

    private static int get(CommandContext<CommandSourceStack> ctx) {
        List<String> lines = new ArrayList<>();
        lines.add("附魔书上限 = " + ModConfig.Cache.bookSize);
        lines.add("药水上限 = " + ModConfig.Cache.potionSize);
        lines.add("血液瓶上限 = " + ModConfig.Cache.bloodSize);
        lines.add("工具上限 = " + ModConfig.Cache.toolSize);
        lines.add("子弹算法 = " + ModConfig.Cache.ammoMode
                + "（倍数 " + ModConfig.Cache.ammoMultiplier
                + "，统一值 " + ModConfig.Cache.ammoFixed
                + "，缺省原上限 " + ModConfig.Cache.ammoFallback + "）");
        lines.add("原版工具名单 = " + ModConfig.Cache.vanillaMode + " " + ModConfig.Cache.vanillaList);
        lines.add("GTM 工具名单 = " + ModConfig.Cache.gtmMode + " " + ModConfig.Cache.gtmList);
        lines.add("子弹名单 = " + ModConfig.Cache.ammoListMode + " " + ModConfig.Cache.ammoList);
        lines.add("按弹种单独指定 = " + ModConfig.Cache.ammoOverrides);
        lines.add("额外清单 = 固定 " + ModConfig.Cache.extraFixed + "，倍数 " + ModConfig.Cache.extraMultiplier);
        ctx.getSource().sendSuccess(() -> Component.literal(String.join("\n", lines)), false);
        return 1;
    }

    private static int help(CommandContext<CommandSourceStack> ctx) {
        List<String> lines = List.of(
                "Bigger Stacks Extras 帮助",
                "数值：books / potions / blood / tools / ammo_multiplier / ammo_fixed，1 = 原版。",
                "子弹算法：/bsextras set ammo_mode multiplier|fixed",
                "按弹种指定：/bsextras set ammo tacz:9mm 800",
                "额外物品：/bsextras set extra minecraft:ender_pearl 64",
                "黑白名单：/bsextras list tools|gtm|ammo mode off|blacklist|whitelist",
                "名单加物品：/bsextras list tools add（手持物品）或 add minecraft:diamond_pickaxe",
                "名单删物品：/bsextras list tools remove minecraft:diamond_pickaxe",
                "看名单：/bsextras list tools show",
                "每一项的详细说明：/bsextras help <key>");
        ctx.getSource().sendSuccess(() -> Component.literal(String.join("\n", lines)), false);
        return 1;
    }

    private static int helpKey(CommandContext<CommandSourceStack> ctx) {
        String key = StringArgumentType.getString(ctx, "key").toLowerCase();
        String text = switch (key) {
            case "books" -> "附魔书一叠多少个。当前 " + ModConfig.Cache.bookSize + "。1 = 原版。";
            case "potions" -> "药水/水瓶/喷溅/滞留一叠多少个。当前 " + ModConfig.Cache.potionSize + "。";
            case "blood" -> "Butchery 血液瓶/感染血液瓶一叠多少个。当前 " + ModConfig.Cache.bloodSize + "。";
            case "tools" -> "工具/武器一叠多少个。当前 " + ModConfig.Cache.toolSize + "。不含盔甲、鞘翅、GTM 电动工具。";
            case "ammo_multiplier" -> "每种子弹按自己的原上限乘这个数。当前 " + ModConfig.Cache.ammoMultiplier + "。";
            case "ammo_fixed" -> "子弹全部统一堆这个数（需要先 ammo_mode fixed）。当前 " + ModConfig.Cache.ammoFixed + "。";
            case "ammo_mode" -> "子弹算法：multiplier 或 fixed。当前 " + ModConfig.Cache.ammoMode + "。";
            case "tools_list" -> "原版工具黑白名单：off / blacklist / whitelist。当前 " + ModConfig.Cache.vanillaMode + "。";
            case "gtm_list" -> "GTM 工具黑白名单。当前 " + ModConfig.Cache.gtmMode + "。";
            case "ammo_list" -> "TACZ 子弹黑白名单（按弹种 id，不是物品 id）。当前 " + ModConfig.Cache.ammoListMode + "。";
            case "extra" -> "额外物品清单，优先于其他规则。当前固定 " + ModConfig.Cache.extraFixed + "，倍数 " + ModConfig.Cache.extraMultiplier + "。";
            default -> "没有这个 key。用 /bsextras help 看全部。";
        };
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static List<String> helpKeys() {
        return List.of("books", "potions", "blood", "tools", "ammo_mode", "ammo_multiplier", "ammo_fixed",
                "tools_list", "gtm_list", "ammo_list", "extra");
    }

    private static int setInt(CommandContext<CommandSourceStack> ctx, ForgeConfigSpec.IntValue value, String label) {
        int newValue = IntegerArgumentType.getInteger(ctx, "value");
        value.set(newValue);
        persist();
        ctx.getSource().sendSuccess(() -> Component.literal(label + " = " + newValue), true);
        return 1;
    }

    private static int setAmmoMode(CommandContext<CommandSourceStack> ctx) {
        String mode = StringArgumentType.getString(ctx, "mode").toLowerCase();
        if (!mode.equals("multiplier") && !mode.equals("fixed")) {
            ctx.getSource().sendFailure(Component.literal("只能是 multiplier 或 fixed"));
            return 0;
        }
        ModConfig.AMMO_MODE.set(mode);
        persist();
        ctx.getSource().sendSuccess(() -> Component.literal("子弹算法 = " + mode), true);
        return 1;
    }

    private static int setAmmoOverride(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        List<String> entries = new ArrayList<>(ModConfig.AMMO_OVERRIDES.get());
        entries.removeIf(raw -> raw != null && raw.startsWith(id + "="));
        entries.add(id + "=" + value);
        ModConfig.AMMO_OVERRIDES.set(entries);
        persist();
        ctx.getSource().sendSuccess(() -> Component.literal("弹种 " + id + " 上限 = " + value), true);
        return 1;
    }

    private static int setExtra(CommandContext<CommandSourceStack> ctx) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
        String raw = StringArgumentType.getString(ctx, "value").trim();
        String value;
        try {
            if (raw.startsWith("*")) {
                int mult = Integer.parseInt(raw.substring(1).trim());
                if (mult < 1) throw new NumberFormatException();
                value = "*" + mult;
            } else {
                int fixed = Integer.parseInt(raw);
                if (fixed < 1 || fixed > 1_000_000) throw new NumberFormatException();
                value = Integer.toString(fixed);
            }
        } catch (NumberFormatException e) {
            ctx.getSource().sendFailure(Component.literal("数值要写成 64 或 *4 这种形式"));
            return 0;
        }
        List<String> entries = new ArrayList<>(ModConfig.EXTRA_ENTRIES.get());
        entries.removeIf(line -> line != null && line.startsWith(id + "="));
        entries.add(id + "=" + value);
        ModConfig.EXTRA_ENTRIES.set(entries);
        persist();
        ctx.getSource().sendSuccess(() -> Component.literal("额外物品 " + id + " = " + value), true);
        return 1;
    }

    private static int setMode(CommandContext<CommandSourceStack> ctx, ListCategory category) {
        String mode = StringArgumentType.getString(ctx, "mode").toLowerCase();
        if (!Set.of("off", "blacklist", "whitelist").contains(mode)) {
            ctx.getSource().sendFailure(Component.literal("只能是 off / blacklist / whitelist"));
            return 0;
        }
        modeOf(category).set(mode);
        persist();
        ctx.getSource().sendSuccess(() -> Component.literal(category.label + " 名单模式 = " + mode), true);
        return 1;
    }

    private static int addHeld(CommandContext<CommandSourceStack> ctx, ListCategory category) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack held = player.getMainHandItem();
        String id = category == ListCategory.AMMO ? StackSizes.ammoId(held) : StackSizes.idOf(held);
        if (id == null || id.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal(category == ListCategory.AMMO
                    ? "手上拿的不是 TACZ 子弹（没有 AmmoId）"
                    : "手上没有物品"));
            return 0;
        }
        return add(ctx, category, id);
    }

    private static int addId(CommandContext<CommandSourceStack> ctx, ListCategory category) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
        return add(ctx, category, id.toString());
    }

    private static int add(CommandContext<CommandSourceStack> ctx, ListCategory category, String id) {
        List<String> entries = new ArrayList<>(listOf(category).get());
        if (!entries.contains(id)) {
            entries.add(id);
            listOf(category).set(entries);
            persist();
        }
        ctx.getSource().sendSuccess(() -> Component.literal("已加入 " + category.label + " 名单：" + id), true);
        return 1;
    }

    private static int removeId(CommandContext<CommandSourceStack> ctx, ListCategory category) {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
        List<String> entries = new ArrayList<>(listOf(category).get());
        boolean removed = entries.remove(id.toString());
        if (removed) {
            listOf(category).set(entries);
            persist();
        }
        ctx.getSource().sendSuccess(() -> Component.literal(removed
                ? "已从 " + category.label + " 名单移除：" + id
                : "名单里没有：" + id), true);
        return 1;
    }

    private static int show(CommandContext<CommandSourceStack> ctx, ListCategory category) {
        ctx.getSource().sendSuccess(() -> Component.literal(category.label + "：模式 = "
                + modeOf(category).get() + "\n" + listOf(category).get()), false);
        return 1;
    }

    private static void persist() {
        ModConfig.refresh();
        ModConfig.save();
        BiggerStacksCompat.ensureGlobalMax(ModConfig.ruleMax());
        NetworkHandler.syncAll();
    }

    private static ForgeConfigSpec.ConfigValue<String> modeOf(ListCategory category) {
        return switch (category) {
            case VANILLA -> ModConfig.VANILLA_MODE;
            case GTM -> ModConfig.GTM_MODE;
            case AMMO -> ModConfig.AMMO_LIST_MODE;
        };
    }

    private static ForgeConfigSpec.ConfigValue<List<? extends String>> listOf(ListCategory category) {
        return switch (category) {
            case VANILLA -> ModConfig.VANILLA_LIST;
            case GTM -> ModConfig.GTM_LIST;
            case AMMO -> ModConfig.AMMO_LIST;
        };
    }

    private enum ListCategory {
        VANILLA("原版工具"),
        GTM("GTM 工具"),
        AMMO("TACZ 子弹");

        private final String label;

        ListCategory(String label) {
            this.label = label;
        }
    }
}
