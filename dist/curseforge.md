# CurseForge 上传材料 — Bigger Stacks Extras 1.0.1

## 项目字段（后台直接照填）

- Name: Bigger Stacks Extras
- Class: Mods
- Main category: Addons
- Additional categories: Storage, Utility & QoL
- Environment: Client and Server
- License: MIT
- Game version: 1.20.1
- Mod loader: Forge
- Required dependency: Bigger Stacks (Required Dependency)
- Optional integrations: TACZ (Timeless and Classics Zero), Butchery, GTCEu Modern
- Java: 17

## Summary（英文一行，≤200 字符）

An addon for Bigger Stacks that adds per-category stack sizes for enchanted books, potions, blood bottles, tools and TACZ ammo, editable in-game.

## Description（英文在上，中文在下）

Bigger Stacks Extras is an addon for Bigger Stacks (Minecraft 1.20.1 / Forge) that extends stack-size control to items the base mod cannot handle correctly: enchanted books, potions, Butchery blood bottles, durable tools and TACZ ammo. Every limit can be changed in-game with /bsextras, applies immediately and is synced from the server to clients.

REQUIREMENTS
- Minecraft 1.20.1
- Forge 47.4.10 or newer
- Bigger Stacks 1.20.1 (required; tested with 1.0.5 and 2026.06.17)
- Java 17
- Optional: TACZ, Butchery, GTCEu Modern (each integration loads only when that mod is installed)

FEATURES
- Enchanted books: books with the same enchantment and level stack together (default 16).
- Potions: water, splash and lingering potions stack by potion type (default 16).
- Butchery bottles: blood bottle, infected blood bottle and sulfuric acid bottle stack to 16. Also fixes a Butchery 5.3.1 bug where drinking an infected blood bottle consumed an extra plain blood bottle and skipped its own effect.
- Tools and weapons: vanilla and GTCEu durable tools stack when item, damage and NBT match (default 16). Uses a "use one, split one" model: the first durability loss separates one item from the stack, so durability is never shared or duplicated. Armor, elytra and GTCEu electric tools never stack. Tridents and crossbows are excluded on purpose.
- TACZ ammo: limits apply per ammo type, not one global number. Multiplier mode (default x10: 9mm 600, .338 300, RPG 60) or fixed mode (one value for all).
- Blacklist / whitelist per category (vanilla tools, GTCEu tools, TACZ ammo), plus an extra list for any item id with a fixed size or a multiplier.
- In-game commands with permission level 2, immediate effect, config persistence and server-to-client sync.
- Values range from 1 to 1,000,000; 1 means vanilla behavior.
- Anvil protection: stacked tools cannot be repaired or combined in an anvil, so a repair cost can never be charged once for a whole stack.

COMMANDS
/bsextras get
/bsextras set books|potions|blood|tools <1-1000000>
/bsextras set ammo_mode multiplier|fixed
/bsextras set ammo_multiplier <1-10000>
/bsextras set ammo_fixed <1-1000000>
/bsextras set ammo <ammo_type_id> <1-1000000>
/bsextras set extra <item_id> <number>   (prefix the number with an asterisk for a multiplier, e.g. asterisk+4 = 4x the vanilla limit)
/bsextras list tools|gtm|ammo mode off|blacklist|whitelist
/bsextras list tools|gtm|ammo add [item_id]   (without an id: uses the item in your hand)
/bsextras list tools|gtm|ammo remove <id>
/bsextras list tools|gtm|ammo show
/bsextras help
/bsextras help <key>

Examples:
/bsextras set books 64
/bsextras set ammo tacz:9mm 800
/bsextras set extra minecraft:ender_pearl 64
/bsextras list ammo mode blacklist
/bsextras list ammo add tacz:9mm

CONFIGURATION
File: config/biggerstacksextras-common.toml
General keys: enchanted_book_size, potion_size, blood_bottle_size, tool_size, ammo_mode, ammo_multiplier, ammo_fixed, ammo_fallback_base, ammo_overrides, extra_entries.
Sections: [tools_vanilla] list_mode + list, [tools_gtm] list_mode + list, [tacz_ammo] list_mode + list.
Defaults: books 16, potions 16, blood bottles 16, tools 16, ammo multiplier 10, ammo fixed 600, fallback base 64, all lists off.

INSTALLATION
1. Install Minecraft Forge 47.4.10 for 1.20.1.
2. Put Bigger Stacks and Bigger Stacks Extras into the mods folder.
3. Launch the game and join a world.
4. Run /bsextras get to confirm the current values.
5. Change values with /bsextras set ...; no restart is needed.
Upgrading from 1.0.0: replace the old jar, keep the config file; 1.0.1 reads the same keys.

IMPORTANT
- Never remove Bigger Stacks or this mod while you have stacks larger than 64. The excess above 127 is lost when the world loads, and this is irreversible. Split large stacks before uninstalling.
- Tweakerge 0.1.5 cancels ItemStack.getMaxStackSize at HEAD and blocks other stack-size mods. 1.0.1 applies its mixin before Tweakerge (mixinPriority 900) and adds an Item-level fallback, so the values apply correctly.
- Tridents and crossbows are always excluded: a trident throw would duplicate the stack, and crossbow loading state is stored on the stack itself.
- GTCEu electric tools always stay at 1 because their charge is stored on the shared stack.
- Butchery code and assets are not used; only item ids are referenced. This mod is not affiliated with Bigger Stacks, TACZ, Butchery or GTCEu.

LICENSE / SOURCE
MIT License. Source: https://github.com/Lin1848624/Biggerstacksextras

---

Bigger Stacks Extras 是 Bigger Stacks 的附属模组（Minecraft 1.20.1 / Forge），把堆叠上限的控制扩展到本体处理不好的物品：附魔书、药水、Butchery 的血液瓶、有耐久的工具，以及 TACZ 的子弹。所有数值都能在游戏里用 /bsextras 修改，立即生效，并由服务端同步给客户端。

前置要求
- Minecraft 1.20.1
- Forge 47.4.10 或更新
- Bigger Stacks 1.20.1（必需；已在 1.0.5 和 2026.06.17 上测试）
- Java 17
- 可选：TACZ、Butchery、GTCEu Modern（装了才启用对应兼容，不装不影响启动）

功能
- 附魔书：附魔种类和等级相同的书叠在一起（默认 16）。
- 药水：水瓶、喷溅药水、滞留药水按药水种类叠（默认 16）。
- Butchery 瓶子：血液瓶、感染血液瓶、硫酸瓶叠到 16。同时修掉 Butchery 5.3.1 的一个 bug：喝感染血液瓶时会多扣一个普通血液瓶，而且不触发感染瓶自己的效果。
- 工具和武器：原版与 GTCEu 的有耐久工具，在物品、耐久、NBT 完全相同时才叠（默认 16）。采用“用一把、拆一把”的方式：第一次掉耐久时，只把其中一把拆出来单独扣耐久，不会共享或复制耐久。盔甲、鞘翅、GTCEu 电动工具永远不叠。三叉戟和十字弓硬排除。
- TACZ 子弹：按弹种分别设上限，不是一个全局数字。倍数模式（默认 x10：9mm 600、.338 300、火箭 60）或统一值模式（所有子弹一个数）。
- 每个类别都有黑名单/白名单（原版工具、GTCEu 工具、TACZ 子弹），另有“额外清单”可以给任意物品 id 设固定值或倍数。
- 游戏内命令，权限等级 2，立即生效，写回配置文件，并从服务端同步到客户端。
- 数值范围 1–1,000,000；填 1 就是原版行为。
- 铁砧保护：叠起来的工具不能在铁砧里修理或合并，避免“收一次修理费、修一整叠”。

命令
/bsextras get
/bsextras set books|potions|blood|tools <1-1000000>
/bsextras set ammo_mode multiplier|fixed
/bsextras set ammo_multiplier <1-10000>
/bsextras set ammo_fixed <1-1000000>
/bsextras set ammo <弹种id> <1-1000000>
/bsextras set extra <物品id> <数字>   （倍数写法：数字前加星号，星号4 表示原版 64 的 4 倍）
/bsextras list tools|gtm|ammo mode off|blacklist|whitelist
/bsextras list tools|gtm|ammo add [物品id]   （不写 id 就用手里拿的物品）
/bsextras list tools|gtm|ammo remove <id>
/bsextras list tools|gtm|ammo show
/bsextras help
/bsextras help <key>

例子：
/bsextras set books 64
/bsextras set ammo tacz:9mm 800
/bsextras set extra minecraft:ender_pearl 64
/bsextras list ammo mode blacklist
/bsextras list ammo add tacz:9mm

配置
文件：config/biggerstacksextras-common.toml
通用项：enchanted_book_size、potion_size、blood_bottle_size、tool_size、ammo_mode、ammo_multiplier、ammo_fixed、ammo_fallback_base、ammo_overrides、extra_entries。
分节：[tools_vanilla] list_mode + list、[tools_gtm] list_mode + list、[tacz_ammo] list_mode + list。
默认值：书 16、药水 16、血瓶 16、工具 16、子弹倍数 10、统一值 600、缺省原上限 64，三个名单都是 off。

安装
1. 给 1.20.1 装 Forge 47.4.10。
2. 把 Bigger Stacks 和 Bigger Stacks Extras 一起放进 mods 文件夹。
3. 启动游戏，进世界。
4. 输入 /bsextras get 确认当前数值。
5. 用 /bsextras set ... 改数值，不用重启。
从 1.0.0 升级：替换旧 jar，配置文件保留即可；1.0.1 读的是同一批 key。

重要
- 堆叠超过 64 时，绝对不要卸载 Bigger Stacks 或本模组。读档时 127 以上的部分会丢，且不可恢复。卸载前先把大堆拆开。
- Tweakerge 0.1.5 会在 ItemStack.getMaxStackSize 最前面取消，挡住其他堆叠模组。1.0.1 用更低的 mixinPriority（900）抢在它前面，并加了 Item 层兜底，所以数值能正常生效。
- 三叉戟和十字弓永远排除：三叉戟投掷会把整叠复制进投射物，十字弓的装填状态记在整叠上。
- GTCEu 电动工具永远是 1，因为电量存在共享的整叠 NBT 里。
- 没有使用 Butchery 的代码或素材，只引用了物品 id。本模组与 Bigger Stacks、TACZ、Butchery、GTCEu 的官方团队无关。

许可证 / 源码
MIT 许可证。源码：https://github.com/Lin1848624/Biggerstacksextras

## Changelog（文件页填这个）

1.0.1
Added
- First public release.
- Per-category stack limits for enchanted books, potions, Butchery bottles, tools and TACZ ammo.
- In-game /bsextras commands with server-to-client sync.
Changed
- Mixin priority 900 so the mod applies before Tweakerge 0.1.5.
- Added an Item.getMaxStackSize fallback for other mods that cancel at HEAD.
Fixed
- An empty or invalid config no longer forces limits to 1.
- Butchery infected blood bottle now consumes only itself and runs its own effect.
