# 测试清单（用户侧）

装之前先备份世界。测试时如果崩溃，把 `logs/latest.log` 和 `crash-reports/` 里最新的文件发我。

## 第 1 轮：启动与配置

1. `mods/` 里放 Bigger Stacks + 本模组，启动。
2. 进世界，输入 `/bsextras get`，应看到 4 个数值 + 子弹算法 + 3 个名单。
3. 日志里搜 `[bsextras]`，应看到：
   - `[bsextras] 正在初始化（mixin 优先级 100）`
   - `[bsextras] 配置: 书=16 药水=16 血瓶=16 工具=16 ...`
   - `[bsextras] 发送配置给 ...`
4. `/bsextras set books 16`，应提示“附魔书上限 = 16”。
5. 退出重进，`/bsextras get` 仍是 16（说明写盘成功）。

## 第 2 轮：书、药水、血瓶

1. `/give @s minecraft:enchanted_book 16` → 应是一叠 16（不是 16 个单独物品）。
2. 两本不同附魔的书 → 不应并在一起。
3. `/give @s minecraft:potion{Potion:"minecraft:water"} 16` → 一叠 16；喝一瓶 → 15 + 玻璃瓶。
4. 附魔台放一叠书 → 只能放进去 1 本（原版槽位限制），附魔正常。
5. 有 Butchery 时：`/give @s butchery:bottle_of_blood 16`、`/give @s butchery:bottle_of_infected_blood 16` → 各自一叠。
6. 喝一瓶感染血液瓶：应触发感染瓶自己的效果，背包里的普通血液瓶数量不应减少。

## 第 3 轮：TACZ

1. `/give @s tacz:ammo{AmmoId:"tacz:9mm"} 60`，再给几组，丢在地上捡起来 → 应并成一叠（上限 600）。
2. 拿枪装弹 → 正常消耗，弹匣数正常。
3. 退出重进 → 600 发的堆叠还在（存盘正常）。
4. `/bsextras set ammo_multiplier 20` → 9mm 变成 1200；再设回 10。

## 第 4 轮：工具（原版）

1. 工作台做 5 把钻石镐 → 应并成一叠 5。
2. 挖一个方块 → 手上变成 1 把（耐久 −1），背包里另外 4 把仍是全新。
3. 把那把用过的和全新的放一起 → 不应并。
4. 铁砧左边放一叠 5 把、右边放 1 把 → 结果栏应为空；拆成 1 把再修 → 正常。
5. `/bsextras list tools mode blacklist`，`/bsextras list tools add`（手持钻石镐）→ 钻石镐上限变 1；`mode off` 恢复。

## 第 5 轮：GTM（如果装了）

1. `/give @s gtceu:lv_wrench` 若干 → 应能并（同材料同工具）。
2. 用扳手转机器 → 只掉手上那把的耐久，其余回背包。
3. 电动工具（如 `gtceu:lv_drill`）→ 不应并（上限 1）。
4. `/bsextras list gtm mode whitelist` + 只加扳手 → 其他 GT 工具回到 1。

## 回退

1. 先把所有超过 64 的堆叠拆开（尤其 TACZ 子弹）。
2. 删 `mods/biggerstacksextras-1.0.2.jar`。
3. 想彻底干净：删 `config/biggerstacksextras-common.toml`。
