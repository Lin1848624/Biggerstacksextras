# Bigger Stacks Extras（更大堆叠：扩展）

Minecraft 1.20.1 / Forge 47.4.10 的附属模组，给 [Bigger Stacks](https://modrinth.com/mod/biggerstacks) 补上它做不到的几类堆叠：

- 附魔书（同种附魔同等级才并）
- 药水（含水瓶、喷溅、滞留）
- Butchery 的血液瓶 / 感染血液瓶
- TACZ 子弹（按弹种各自的上限，不是全部一个数）
- 工具 / 武器（原版 + GTCEu Modern 的耐久工具；同物品、同耐久、同附魔才并）
- 注：三叉戟、十字弓默认不堆（三叉戟投掷会把整叠复制进弹射物，十字弓的装填状态记在整叠上）。

## 依赖

- **必装**：Bigger Stacks（1.20.1 版，1.0.5 或 2026.06.17 都行）。超过 127 的堆叠要靠它改写存档和联机数据。
- **可选**：TACZ、Butchery、GTCEu Modern。装了才启用对应兼容；没装不会报错，也不会加载它们的类。
- 服务器和客户端都要装本模组（堆叠上限必须两端一致）。

## 安装

1. 把 `biggerstacksextras-1.0.1.jar` 放进 `mods/`。
2. 确认 `mods/` 里已有 Bigger Stacks。
3. 启动一次游戏，生成 `config/biggerstacksextras-common.toml`。
4. 进游戏用 `/bsextras get` 看当前数值；用 `/bsextras set` 改，立即生效。

## 默认值

| 类别 | 默认 | 说明 |
|---|---|---|
| 附魔书 | 16 | 同种同等级才并 |
| 药水 | 16 | 水瓶/喷溅/滞留都算 |
| 血液瓶 | 16 | Butchery 的两种瓶子 |
| 工具 | 16 | 不含盔甲、鞘翅、电动工具 |
| TACZ 子弹 | 每种原上限 ×10 | 9mm 600、.338 300、火箭 60 |

所有数值 1 = 保持原版，最大 1000000。

## 工具堆叠的行为

- 5 把全新钻石镐 → 自动并成一叠 5。
- 第一次真正扣耐久 → 手上那把单独出来（耐久 −1），另外 4 把原样回背包，仍是全新。
- 用过的和全新的数据不同 → 并不回去；两把都是耐久 100 的可以并，100 和 101 不行。
- 附魔、改名、铁砧修过的，数据不同 → 并不进去。
- 铁砧里只要有一边是整叠工具，结果栏就是空的（防“1 件价格修一整叠”）。拆开再修。
- 砂轮不收整叠（原版行为，不吞东西）。
- 附魔台一次只能放 1 件（原版槽位限制，不会整叠附魔）。

## 命令

```
/bsextras get
/bsextras set books 16
/bsextras set potions 16
/bsextras set blood 16
/bsextras set tools 16
/bsextras set ammo_mode multiplier        # 或 fixed
/bsextras set ammo_multiplier 10
/bsextras set ammo_fixed 600
/bsextras set ammo tacz:9mm 800           # 按弹种单独指定
/bsextras set extra minecraft:ender_pearl 64
/bsextras set extra some_mod:some_item *4
/bsextras list tools mode off|blacklist|whitelist
/bsextras list tools add                  # 手持物品，自动取 id
/bsextras list tools add minecraft:diamond_pickaxe
/bsextras list tools remove minecraft:diamond_pickaxe
/bsextras list tools show
/bsextras list gtm ...                    # GTM 工具，同上
/bsextras list ammo ...                   # TACZ 弹种，例如 tacz:9mm
/bsextras help
/bsextras help <key>
```

- 权限：单人/LAN 主机不需要开作弊；专用服务器需要 OP（等级 2）。
- 被黑白名单排除的物品会强制回到原版上限（工具 1、子弹回它自己的原上限），不会被 Bigger Stacks 的通用规则又堆起来。

## 配置项

`config/biggerstacksextras-common.toml`，都有中文注释：

- `enchanted_book_size` / `potion_size` / `blood_bottle_size` / `tool_size`
- `ammo_mode` = `multiplier` 或 `fixed`
- `ammo_multiplier` / `ammo_fixed` / `ammo_fallback_base`
- `ammo_overrides` = `["tacz:9mm=800"]`
- `extra_entries` = `["minecraft:ender_pearl=64", "some_mod:some_item=*4"]`
- `tools_vanilla.list_mode` + `list`
- `tools_gtm.list_mode` + `list`
- `tacz_ammo.list_mode` + `list`（写弹种 id）

改配置文件需要重启；用命令改立即生效并写回文件。

## 已知限制

- TACZ 子弹的“按弹种”只在装了 TACZ 时生效；没装时 `tacz:ammo` 这个 id 不存在，配置里的条目会被忽略。
- GTM 的电动工具（电钻、电锯、电动扳手）不参与堆叠：它们用电，整叠会共用/复制电量。
- 其他模组的工具默认不堆；可以用 `extra_entries` 加，但如果那个模组不用原版扣耐久，可能不会“分出一把”，而是整叠共享耐久。
- 漏斗/管道往酿造台塞整叠药水时，原版酿造逻辑只会留下 1 瓶（手动放 1 瓶没事）；这是原版+大堆叠的通病，第一版不处理。
- 三叉戟、十字弓固定不堆：三叉戟投掷时会把整叠复制进弹射物实体，十字弓的装填状态记在整叠上。这一条不在配置里开放。
- 卸载本模组或 Bigger Stacks 前，先把超过 64 的堆叠拆开；超过 127 的部分会丢。这是唯一不可逆的点。
- 装了 Butchery 5.3.1 时，本模组会顺手修一个它自己的 bug：喝感染血液瓶不再额外扣普通血液瓶，并触发感染瓶本来的效果。

## 和 Tweakerge 的冲突（1.0.1 已处理）

Tweakerge 0.1.5 在 `ItemStack.getMaxStackSize` 的最前面直接取消，返回“物品自己的原版上限”。它会盖掉所有堆叠模组（包括 Bigger Stacks 和本模组），表现就是“装了但什么都没变”。

本模组的 1.0.1 用更低的 mixin 配置优先级（100）抢在它前面，另外在 `Item.getMaxStackSize()` 也加了一层兜底。如果你还看到上限没变，先看日志里有没有这一行：

```
[bsextras] 配置: 书=16 药水=16 ...
```

没有这一行说明配置事件没触发；有这一行但数值不对，把日志发我。

## 构建

```
./gradlew build
```

产物：`build/libs/biggerstacksextras-1.0.1.jar`。
