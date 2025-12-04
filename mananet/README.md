# Mananet 节点系统（抽象层）

这里收敛网络机器（方块/方块实体/注册工具）的抽象与规范，代码在 `src/main/java/org/creepebucket/programmable_magic/mananet` 包下。

- AbstractNetworkNode：定义节点的 tick 行为、需要的 DataComponentType 列表，以及注册元数据（注册名、模型、贴图等）。
- NetworkNodeRegistrar：将一个 AbstractNetworkNode 一键注册为方块 + 方块实体（可选方块物品）。
- NodeBoundBlock / NodeBoundBlockEntity：通用承载实现，自动把 Ticker 回调到节点的 `tick`。

使用：
1) 定义一个节点类继承 AbstractNetworkNode，返回 NodeRegistryData（name/本地化键/资源路径/是否注册方块物品）。
2) 在公共注册阶段调用 `NetworkNodeRegistrar.register(new YourNode())` 完成注册。

说明：
- 抽象层不包含模型与 blockstate 细节；资源生成或 JSON 交由数据生成器或独立流程处理。
- 方块连接逻辑（网络拓扑触发）来自基础类 `AbstractConnectMachineBlock` / `AbstractConnectMachineBlockEntity`。
