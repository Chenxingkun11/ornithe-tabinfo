# Ornithe Tab Info

[中文](#中文) | [English](#english)

当前版本 / Current version: **1.0.0** · Minecraft **1.7.10** · Ornithe **gen1** (Fabric Loader) + OSL · MIT

本分支 `gen1/fabric` 面向 Ornithe gen1 环境；gen2 版本在 `gen2/fabric` 分支。
This `gen1/fabric` branch targets Ornithe gen1; the gen2 build lives on the `gen2/fabric` branch.

---

## 中文

按住 TAB 时，在原版玩家列表上方显示服务端的自动保存倒计时、上次自动保存耗时、TPS 和 MSPT。

```text
Ornithe Tab Info
Autosave: 580gt | Prev: 0.8gt
TPS: 20.0 | MSPT: 12.5ms
```

### 字段说明

| 字段 | 单位 | 含义 |
|---|---|---|
| `Autosave` | gt（服务端游戏刻） | 距离下一次原版自动保存还剩多少个服务端 tick。1.7.10 每 900gt 自动保存一次，数值为 `900 - ticks % 900`，保存完成的那一刻回到 `900gt`。 |
| `Prev` | gt（由真实耗时换算） | 上一次自动保存（玩家数据 + 全部世界区块）用 `System.nanoTime()` 测得的真实耗时，除以 50 ms 换算成标准游戏刻，保留一位小数。首次自动保存前显示 `--`。 |
| `TPS` | 次/秒 | 最近 100 个 tick 起点之间的实际间隔换算出的 tick 频率。不做 20 的上限截断，服务器卡顿后追帧时可能短暂高于 20。 |
| `MSPT` | 毫秒 | 最近 100 个 tick 中 `MinecraftServer.tick()` 本身的平均耗时。 |

服务端每 20 个 tick（约 1 秒）推送一次快照，因此 `Autosave` 以 20gt 为步长变化。

### 不同安装方式的行为

| 安装方式 | 行为 |
|---|---|
| 客户端 + 服务端都安装 | 显示服务端的真实 Autosave、Prev、TPS、MSPT。 |
| 单人游戏 | 集成服务端运行同样的采样逻辑，显示真实数据。暂停期间不计入 TPS。 |
| 仅客户端安装 | 可以正常进服、打开 TAB，面板显示 `Server data unavailable`，不会用客户端时间去猜测任何数值。 |
| 仅服务端安装 | 正常采样，但只向声明了 `ornithetabinfo:stats` 频道的客户端发包；原版客户端可以正常登录，不会收到未知频道数据。 |

客户端超过 3 秒没有收到新快照就视为过期，同样显示 `Server data unavailable`。数据与收到它的网络连接绑定，断线、重连或切换服务器都不会显示上一个服务器的旧数据。

在单人游戏里只有自己且没有 TAB 计分项时，原版不会绘制玩家列表；本 Mod 仍然会在按住 TAB 时显示面板。

### 运行要求

- Minecraft 1.7.10
- 以 **gen1** 中间名映射安装的 Ornithe Fabric Loader（开发时使用 0.19.5，`fabric.mod.json` 要求 `>=0.17.3`）
- gen1 版 OSL（Ornithe Standard Libraries），开发时使用 0.16.3；需要其中的 `osl-entrypoints >=0.4.3` 与 `osl-networking >=0.8.0`
- gen1 与 gen2 的 JAR 不能混用：gen2 版 OSL（如 0.21.0、0.23.0）和 gen2 版本 Mod 在 gen1 环境里都无法生效
- 运行时 Java 8 即可（字节码目标为 Java 8）

### 构建

构建需要 JDK 25。仓库中的 `gradle/gradle-daemon-jvm.properties` 会让 Gradle 自动在本机寻找 JDK 25 作为守护进程，即使默认 `java` 是其他版本也可以直接构建。

macOS / Linux：

```bash
./gradlew build
```

Windows：

```bat
gradlew.bat build
```

产物位于 `build/libs/`：

- `ornithe-tabinfo-1.0.0+mc1.7.10.jar`：可安装的 Mod
- `ornithe-tabinfo-1.0.0+mc1.7.10-sources.jar`：源码包

### 安装

客户端：

1. 用 Ornithe 安装器为 Minecraft 1.7.10 安装 gen1 的 Ornithe（Fabric Loader）。
2. 把 gen1 版 OSL 和 `ornithe-tabinfo-1.0.0+mc1.7.10.jar` 放进该实例的 `mods` 文件夹。
3. 启动游戏，进入世界或服务器后按住 TAB。

服务端：

1. 为 1.7.10 服务端安装 gen1 的 Ornithe（Fabric Loader）。
2. 把 gen1 版 OSL 和同一个 `ornithe-tabinfo-1.0.0+mc1.7.10.jar` 放进服务端的 `mods` 文件夹。
3. 重启服务端。

客户端和服务端使用同一个 JAR。Ornithe 与 OSL 的获取方式见 [ornithemc.net](https://ornithemc.net)。

### 架构

```text
[服务端] MinecraftServer.tick()
    └─ MinecraftServerMixin → ServerMetricsTracker
          │  每 20 tick 一次，OSL 频道 ornithetabinfo:stats
          │  （只发往声明了该频道的客户端）
          ▼
[客户端] OrnitheTabInfoClient → TabInfoClientState（按连接隔离，3 秒过期）
    └─ GameGuiMixin → TabInfoHudRenderer
```

- `mixin/server/MinecraftServerMixin`：在 `MinecraftServer.tick()` 的开头和结尾记录 tick 耗时与间隔；在自动保存分支的 `PlayerManager.saveAll()` 之前开始计时、`saveWorlds(true)` 之后结束计时。
- `mixin/client/IntegratedServerMixin`：单人游戏暂停时通知采样器丢弃跨越暂停的那段间隔，避免恢复后 TPS 被拉低。
- `mixin/client/GameGuiMixin`：注入 Feather 映射下的 `GameGui.render(float, boolean, int, int)`，把玩家列表的起始 Y 从 `10` 改为面板下方（有数据时为 `38`，不可用时为 `29`），并在按住 TAB 时绘制居中的半透明面板。
- 网络同步使用 OSL：客户端注册 `ornithetabinfo:stats` 监听器，即在 OSL 握手中声明该频道；服务端用 `ServerPlayNetworking.send(channel, …)`，它只发送给声明过该频道的玩家。
- Mixin 配置中 `defaultRequire` 为 1：如果 Feather 注入点不匹配，游戏会在启动或加载时直接报错，而不是静默缺失功能。

协议版本 1 的字段顺序：

| 顺序 | 类型 | 字段 |
|---|---|---|
| 1 | `byte` | `protocolVersion`（= 1） |
| 2 | `int` | `serverTick` |
| 3 | `int` | `autosaveRemainingGt` |
| 4 | `float` | `previousSaveGt`（首次保存前为 `NaN`） |
| 5 | `float` | `tps` |
| 6 | `float` | `mspt` |

客户端收到不认识的协议版本时会忽略该包并在日志中警告一次。

v1 不包含配置界面、历史图表、命令或权限系统。

### 许可证

本项目以 [MIT 许可证](LICENSE) 发布。

---

## English

Hold TAB to see the server's autosave countdown, last autosave duration, TPS and MSPT above the vanilla player list.

```text
Ornithe Tab Info
Autosave: 580gt | Prev: 0.8gt
TPS: 20.0 | MSPT: 12.5ms
```

### Fields

| Field | Unit | Meaning |
|---|---|---|
| `Autosave` | gt (server game ticks) | Server ticks left until the next vanilla autosave. 1.7.10 autosaves every 900gt; the value is `900 - ticks % 900` and returns to `900gt` on the tick the save completes. |
| `Prev` | gt (converted from real time) | Real duration of the previous autosave (player data plus all world chunks), measured with `System.nanoTime()` and divided by 50 ms to express it in standard game ticks, one decimal place. Shows `--` until the first autosave. |
| `TPS` | ticks/second | Actual tick rate from the real intervals between the last 100 tick starts. Not capped at 20, so it can briefly exceed 20 while the server catches up after lag. |
| `MSPT` | milliseconds | Mean time spent inside `MinecraftServer.tick()` over the last 100 ticks. |

The server pushes a snapshot every 20 ticks (about once per second), so `Autosave` moves in steps of 20gt.

### Behavior by install

| Installed on | Behavior |
|---|---|
| Client and server | Shows the server's real Autosave, Prev, TPS and MSPT. |
| Singleplayer | The integrated server runs the same sampling and shows real data. Time spent paused does not count against TPS. |
| Client only | Joining servers and opening TAB work normally; the panel shows `Server data unavailable` and never guesses values from client time. |
| Server only | Sampling runs, but packets go only to clients that declared the `ornithetabinfo:stats` channel. Vanilla clients log in normally and never receive unknown channel data. |

If no new snapshot arrives for 3 seconds, the data is treated as stale and the panel shows `Server data unavailable`. Data is tied to the connection it arrived on, so disconnecting, reconnecting or switching servers never shows the previous server's values.

In singleplayer with no other players and no TAB objective, vanilla does not draw the player list at all; this mod still shows its panel while TAB is held.

### Requirements

- Minecraft 1.7.10
- Ornithe Fabric Loader installed with **gen1** intermediary (developed against 0.19.5; `fabric.mod.json` requires `>=0.17.3`)
- The gen1 build of OSL (Ornithe Standard Libraries), developed against 0.16.3; needs `osl-entrypoints >=0.4.3` and `osl-networking >=0.8.0`
- gen1 and gen2 JARs do not mix: gen2 OSL (e.g. 0.21.0, 0.23.0) and the gen2 build of this mod do not work in a gen1 environment
- Java 8 at runtime (bytecode targets Java 8)

### Building

Building needs JDK 25. `gradle/gradle-daemon-jvm.properties` makes Gradle find a local JDK 25 for its daemon, so the build works even when your default `java` is a different version.

macOS / Linux:

```bash
./gradlew build
```

Windows:

```bat
gradlew.bat build
```

Output in `build/libs/`:

- `ornithe-tabinfo-1.0.0+mc1.7.10.jar`: the mod to install
- `ornithe-tabinfo-1.0.0+mc1.7.10-sources.jar`: sources

### Installing

Client:

1. Install gen1 Ornithe (Fabric Loader) for Minecraft 1.7.10 with the Ornithe installer.
2. Put the gen1 build of OSL and `ornithe-tabinfo-1.0.0+mc1.7.10.jar` in the instance's `mods` folder.
3. Launch the game, join a world or server, and hold TAB.

Server:

1. Install gen1 Ornithe (Fabric Loader) on the 1.7.10 server.
2. Put the gen1 build of OSL and the same `ornithe-tabinfo-1.0.0+mc1.7.10.jar` in the server's `mods` folder.
3. Restart the server.

Client and server use the same JAR. See [ornithemc.net](https://ornithemc.net) for getting Ornithe and OSL.

### Architecture

```text
[server] MinecraftServer.tick()
    └─ MinecraftServerMixin → ServerMetricsTracker
          │  every 20 ticks, OSL channel ornithetabinfo:stats
          │  (only to clients that declared the channel)
          ▼
[client] OrnitheTabInfoClient → TabInfoClientState (per connection, 3 s expiry)
    └─ GameGuiMixin → TabInfoHudRenderer
```

- `mixin/server/MinecraftServerMixin`: records tick duration and interval at the start and end of `MinecraftServer.tick()`; times the autosave from just before `PlayerManager.saveAll()` to just after `saveWorlds(true)` in the autosave branch.
- `mixin/client/IntegratedServerMixin`: when singleplayer is paused, tells the tracker to drop the interval spanning the pause so TPS is not dragged down after resuming.
- `mixin/client/GameGuiMixin`: injects into Feather's `GameGui.render(float, boolean, int, int)`, moves the player list's starting y from `10` to just below the panel (`38` with data, `29` when unavailable), and draws the centered translucent panel while TAB is held.
- Networking uses OSL: registering the client listener for `ornithetabinfo:stats` declares the channel during the OSL handshake, and the server's `ServerPlayNetworking.send(channel, …)` only sends to players that declared it.
- The mixin config keeps `defaultRequire` at 1: if a Feather injection point does not match, the game fails loudly at startup or class load instead of silently losing features.

Protocol version 1 field order:

| # | Type | Field |
|---|---|---|
| 1 | `byte` | `protocolVersion` (= 1) |
| 2 | `int` | `serverTick` |
| 3 | `int` | `autosaveRemainingGt` |
| 4 | `float` | `previousSaveGt` (`NaN` before the first save) |
| 5 | `float` | `tps` |
| 6 | `float` | `mspt` |

A client that receives an unknown protocol version ignores the packet and logs one warning.

v1 has no config screen, history graphs, commands or permission system.

### License

Released under the [MIT License](LICENSE).
