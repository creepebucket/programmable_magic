# ManaNetwork 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/mananet/ManaNetwork.java`  
**包名**: `org.creepebucket.programmable_magic.mananet`  
**设计模式**: 单例模式 + 网络管理器

## 🎯 类设计目的

`ManaNetwork` 是四维魔力系统的**全局网络管理器**，负责协调和管理整个世界的魔力流动、节点连接以及能量平衡。它实现了分布式魔力网络的概念，让魔力可以在不同位置和设备间传输共享。

## 🏗️ 核心数据结构

### 网络节点系统
```java
public class ManaNetwork {
    // 全局单例实例
    private static ManaNetwork INSTANCE;
    
    // 核心数据结构
    private final Map<BlockPos, ManaNode> nodes = new ConcurrentHashMap<>();
    private final Map<UUID, ManaConsumer> consumers = new ConcurrentHashMap<>();
    private final Set<ManaConnection> connections = ConcurrentHashMap.newKeySet();
    
    // 网络状态
    private boolean networkDirty = false;
    private long lastUpdateTime = 0;
    private int updateIntervalTicks = 20; // 每秒更新一次
}
```

### 魔力节点定义
```java
public class ManaNode {
    private final BlockPos position;
    private final Level level;
    private ModUtils.Mana storedMana;        // 节点存储的魔力
    private ModUtils.Mana maxCapacity;       // 最大存储容量
    private NodeType nodeType;               // 节点类型
    private Set<ManaNode> connectedNodes;    // 连接的节点集合
    private long lastAccessTime;             // 最后访问时间
    
    public enum NodeType {
        GENERATOR,    // 魔力生成器
        STORAGE,      // 魔力储存器
        CONSUMER,     // 魔力消费者
        TRANSMITTER   // 魔力传输器
    }
    
    public boolean canTransferTo(ManaNode other) {
        // 检查传输条件
        if (this.storedMana.isEmpty()) return false;
        if (other.storedMana.equals(other.maxCapacity)) return false;
        if (!isConnectedTo(other)) return false;
        
        return true;
    }
}
```

### 连接关系管理
```java
public class ManaConnection {
    private final ManaNode source;
    private final ManaNode target;
    private final ConnectionType type;
    private double transferRate;             // 传输速率
    private double efficiency;               // 传输效率
    private long lastTransferTime;           // 最后传输时间
    
    public enum ConnectionType {
        DIRECT,       // 直接连接
        WIRELESS,     // 无线连接
        CHANNEL       // 通道连接
    }
    
    public ModUtils.Mana transferMana(ModUtils.Mana amount) {
        // 计算实际传输量（考虑效率损失）
        ModUtils.Mana actualTransfer = amount.multiply(efficiency);
        
        // 执行传输
        source.storedMana.subtract(actualTransfer);
        target.storedMana.add(actualTransfer);
        
        lastTransferTime = System.currentTimeMillis();
        return actualTransfer;
    }
}
```

## 🔧 核心方法详解

### 网络管理方法

#### `registerNode()` - 节点注册
```java
public void registerNode(BlockPos pos, Level level, ManaNode node) {
    // 验证节点位置唯一性
    if (nodes.containsKey(pos)) {
        throw new IllegalArgumentException("Node already exists at position: " + pos);
    }
    
    // 验证维度兼容性
    if (!isDimensionSupported(level)) {
        throw new IllegalArgumentException("Dimension not supported: " + level.dimension());
    }
    
    nodes.put(pos, node);
    networkDirty = true;
    
    // 自动寻找附近节点建立连接
    establishNearbyConnections(node);
    
    LOGGER.info("Registered mana node at {} in {}", pos, level.dimension().location());
}

private void establishNearbyConnections(ManaNode node) {
    final int CONNECTION_RANGE = 16; // 16格连接范围
    
    for (ManaNode nearby : getNodesInRange(node.getPosition(), CONNECTION_RANGE)) {
        if (canNodesConnect(node, nearby)) {
            createConnection(node, nearby, ConnectionType.DIRECT);
        }
    }
}
```

#### `unregisterNode()` - 节点注销
```java
public void unregisterNode(BlockPos pos) {
    ManaNode node = nodes.remove(pos);
    if (node == null) return;
    
    // 断开所有连接
    disconnectNode(node);
    
    // 通知相关消费者
    notifyConsumersNodeRemoved(node);
    
    networkDirty = true;
    LOGGER.info("Unregistered mana node at {}", pos);
}

private void disconnectNode(ManaNode node) {
    // 移除涉及此节点的所有连接
    connections.removeIf(conn -> 
        conn.getSource() == node || conn.getTarget() == node
    );
    
    // 从其他节点的连接列表中移除
    for (ManaNode other : nodes.values()) {
        other.getConnectedNodes().remove(node);
    }
}
```

### 魔力传输系统

#### `transferManaBetweenNodes()` - 节点间传输
```java
public ModUtils.Mana transferManaBetweenNodes(ManaNode source, ManaNode target, 
                                            ModUtils.Mana requestedAmount) {
    // 验证传输可行性
    if (!source.canTransferTo(target)) {
        return new ModUtils.Mana(0, 0, 0, 0);
    }
    
    // 计算最大可传输量
    ModUtils.Mana available = source.getStoredMana();
    ModUtils.Mana space = target.getMaxCapacity().subtract(target.getStoredMana());
    ModUtils.Mana maxTransfer = available.min(space);
    
    // 应用传输限制
    ModUtils.Mana actualTransfer = requestedAmount.min(maxTransfer);
    
    // 执行传输
    source.getStoredMana().subtract(actualTransfer);
    target.getStoredMana().add(actualTransfer);
    
    // 记录传输历史
    recordTransfer(source, target, actualTransfer);
    
    networkDirty = true;
    return actualTransfer;
}

private void recordTransfer(ManaNode source, ManaNode target, ModUtils.Mana amount) {
    TransferRecord record = new TransferRecord(
        source.getPosition(),
        target.getPosition(),
        amount,
        System.currentTimeMillis()
    );
    
    transferHistory.add(record);
    
    // 限制历史记录数量
    if (transferHistory.size() > MAX_HISTORY_SIZE) {
        transferHistory.poll(); // 移除最老的记录
    }
}
```

#### `balanceNetworkMana()` - 网络魔力平衡
```java
public void balanceNetworkMana() {
    if (!networkDirty && System.currentTimeMillis() - lastUpdateTime < 1000) {
        return; // 避免频繁平衡
    }
    
    // 收集所有生成器和储存器
    List<ManaNode> generators = getNodesByType(NodeType.GENERATOR);
    List<ManaNode> storages = getNodesByType(NodeType.STORAGE);
    List<ManaNode> consumers = getNodesByType(NodeType.CONSUMER);
    
    // 生成器向储存器充电
    balanceGeneratorToStorage(generators, storages);
    
    // 储存器向消费者供电
    balanceStorageToConsumer(storages, consumers);
    
    // 消费者之间的负载均衡
    balanceConsumerLoad(consumers);
    
    lastUpdateTime = System.currentTimeMillis();
    networkDirty = false;
}

private void balanceGeneratorToStorage(List<ManaNode> generators, List<ManaNode> storages) {
    for (ManaNode generator : generators) {
        if (generator.getStoredMana().anyGreaterThan(generator.getMaxCapacity().multiply(0.8))) {
            // 发电量充足时向储存器充电
            distributeMana(generator, storages, 0.1); // 10%传输率
        }
    }
}

private void balanceStorageToConsumer(List<ManaNode> storages, List<ManaNode> consumers) {
    for (ManaNode consumer : consumers) {
        if (consumer.getStoredMana().anyLessThan(consumer.getMaxCapacity().multiply(0.2))) {
            // 消费者魔力不足时从储存器取电
            requestMana(consumer, storages, 0.15); // 15%传输率
        }
    }
}
```

### 查询和统计方法

#### `getNodeStatus()` - 节点状态查询
```java
public NodeStatus getNodeStatus(BlockPos pos) {
    ManaNode node = nodes.get(pos);
    if (node == null) {
        return null;
    }
    
    return new NodeStatus(
        node.getPosition(),
        node.getNodeType(),
        node.getStoredMana(),
        node.getMaxCapacity(),
        node.getConnectedNodes().size(),
        calculateEfficiency(node),
        System.currentTimeMillis() - node.getLastAccessTime()
    );
}

public static class NodeStatus {
    private final BlockPos position;
    private final ManaNode.NodeType type;
    private final ModUtils.Mana currentMana;
    private final ModUtils.Mana maxMana;
    private final int connectionCount;
    private final double efficiency;
    private final long idleTimeMs;
    
    // 构造函数和getter方法...
}
```

#### `getNetworkStatistics()` - 网络统计信息
```java
public NetworkStatistics getNetworkStatistics() {
    return new NetworkStatistics(
        nodes.size(),
        connections.size(),
        getTotalGeneratedMana(),
        getTotalStoredMana(),
        getTotalConsumedMana(),
        calculateNetworkEfficiency(),
        getAverageTransferRate()
    );
}

public static class NetworkStatistics {
    private final int nodeCount;
    private final int connectionCount;
    private final ModUtils.Mana totalGeneration;
    private final ModUtils.Mana totalStorage;
    private final ModUtils.Mana totalConsumption;
    private final double networkEfficiency;
    private final double avgTransferRate;
    
    public String toDisplayString() {
        return String.format(
            "Mana Network Stats:\n" +
            "Nodes: %d | Connections: %d\n" +
            "Generation: %s\n" +
            "Storage: %s\n" +
            "Consumption: %s\n" +
            "Efficiency: %.1f%% | Avg Rate: %.2f/s",
            nodeCount, connectionCount,
            formatMana(totalGeneration),
            formatMana(totalStorage),
            formatMana(totalConsumption),
            networkEfficiency * 100,
            avgTransferRate
        );
    }
}
```

## 🔄 网络同步机制

### 客户端同步
```java
public class ManaNetworkSync {
    // 同步数据包
    public static class ManaNetworkUpdatePacket extends BasePacket {
        private final Map<BlockPos, NodeSyncData> nodeData;
        private final long timestamp;
        
        @Override
        public void handle(IPayloadContext context) {
            context.enqueueWork(() -> {
                ManaNetwork network = ManaNetwork.getInstance();
                network.updateFromSyncData(nodeData, timestamp);
            });
        }
    }
    
    public void syncToClient(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        Map<BlockPos, NodeSyncData> syncData = collectSyncData();
        ManaNetworkUpdatePacket packet = new ManaNetworkUpdatePacket(syncData, 
                                                                   System.currentTimeMillis());
        
        Networking.sendToPlayer(serverPlayer, packet);
    }
    
    private Map<BlockPos, NodeSyncData> collectSyncData() {
        Map<BlockPos, NodeSyncData> data = new HashMap<>();
        
        for (Map.Entry<BlockPos, ManaNode> entry : nodes.entrySet()) {
            ManaNode node = entry.getValue();
            data.put(entry.getKey(), new NodeSyncData(
                node.getStoredMana(),
                node.getNodeType(),
                new ArrayList<>(node.getConnectedNodes().stream()
                    .map(ManaNode::getPosition)
                    .collect(Collectors.toList()))
            ));
        }
        
        return data;
    }
}
```

### 性能优化更新
```java
public class NetworkUpdater {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
    private final AtomicInteger pendingUpdates = new AtomicInteger(0);
    
    public void startPeriodicUpdates() {
        // 定期平衡更新
        executor.scheduleAtFixedRate(
            this::performBalancingUpdate,
            0, 5, TimeUnit.SECONDS
        );
        
        // 增量同步更新
        executor.scheduleAtFixedRate(
            this::performIncrementalSync,
            0, 1, TimeUnit.SECONDS
        );
    }
    
    private void performBalancingUpdate() {
        if (pendingUpdates.get() > 0) {
            balanceNetworkMana();
            pendingUpdates.set(0);
        }
    }
    
    private void performIncrementalSync() {
        // 只同步变化的数据
        syncChangedNodes();
    }
}
```

## ⚡ 高级功能特性

### 智能路由算法
```java
public class ManaRouting {
    public ModUtils.Mana routeMana(ManaNode source, ManaNode destination, 
                                 ModUtils.Mana amount) {
        // 使用Dijkstra算法寻找最优路径
        List<ManaNode> path = findOptimalPath(source, destination);
        
        if (path.isEmpty()) {
            return new ModUtils.Mana(0, 0, 0, 0);
        }
        
        // 沿路径传输魔力
        ModUtils.Mana remaining = amount.copy();
        for (int i = 0; i < path.size() - 1; i++) {
            ManaNode current = path.get(i);
            ManaNode next = path.get(i + 1);
            
            ModUtils.Mana transferred = transferManaBetweenNodes(current, next, remaining);
            remaining.subtract(transferred);
            
            if (remaining.isEmpty()) break;
        }
        
        return amount.subtract(remaining);
    }
    
    private List<ManaNode> findOptimalPath(ManaNode source, ManaNode target) {
        // 实现改进的Dijkstra算法
        PriorityQueue<PathNode> queue = new PriorityQueue<>();
        Map<ManaNode, PathNode> visited = new HashMap<>();
        
        queue.offer(new PathNode(source, new ModUtils.Mana(0, 0, 0, 0), 0));
        
        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            
            if (current.node == target) {
                return reconstructPath(current);
            }
            
            if (visited.containsKey(current.node) && 
                visited.get(current.node).cost <= current.cost) {
                continue;
            }
            
            visited.put(current.node, current);
            
            // 探索相邻节点
            for (ManaNode neighbor : current.node.getConnectedNodes()) {
                double newCost = current.cost + calculateTransferCost(current.node, neighbor);
                ModUtils.Mana newMana = current.mana.add(
                    transferManaBetweenNodes(current.node, neighbor, 
                                           current.node.getStoredMana())
                );
                
                queue.offer(new PathNode(neighbor, newMana, newCost));
            }
        }
        
        return Collections.emptyList(); // 无路径
    }
}
```

### 网络事件系统
```java
public class ManaNetworkEvents {
    private final List<NetworkEventListener> listeners = new CopyOnWriteArrayList<>();
    
    public interface NetworkEventListener {
        default void onNodeAdded(ManaNode node) {}
        default void onNodeRemoved(ManaNode node) {}
        default void onManaTransferred(ManaNode source, ManaNode target, ModUtils.Mana amount) {}
        default void onNetworkImbalance(NetworkImbalanceEvent event) {}
    }
    
    public void addListener(NetworkEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(NetworkEventListener listener) {
        listeners.remove(listener);
    }
    
    // 事件触发方法
    private void fireNodeAdded(ManaNode node) {
        listeners.forEach(listener -> listener.onNodeAdded(node));
    }
    
    private void fireManaTransferred(ManaNode source, ManaNode target, ModUtils.Mana amount) {
        listeners.forEach(listener -> listener.onManaTransferred(source, target, amount));
    }
}
```

## 🛡️ 安全性和稳定性

### 网络完整性检查
```java
public class NetworkValidator {
    public ValidationResult validateNetwork() {
        List<ValidationError> errors = new ArrayList<>();
        
        // 检查节点一致性
        validateNodeIntegrity(errors);
        
        // 检查连接有效性
        validateConnectionIntegrity(errors);
        
        // 检查魔力守恒
        validateManaConservation(errors);
        
        // 检查循环引用
        validateNoCircularReferences(errors);
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    private void validateNodeIntegrity(List<ValidationError> errors) {
        for (ManaNode node : nodes.values()) {
            if (node.getStoredMana().anyLessThan(new ModUtils.Mana(0, 0, 0, 0))) {
                errors.add(new ValidationError(
                    "Negative mana in node at " + node.getPosition(),
                    ValidationError.Type.DATA_CORRUPTION
                ));
            }
            
            if (node.getStoredMana().anyGreaterThan(node.getMaxCapacity())) {
                errors.add(new ValidationError(
                    "Mana overflow in node at " + node.getPosition(),
                    ValidationError.Type.DATA_CORRUPTION
                ));
            }
        }
    }
}
```

### 自动恢复机制
```java
public class NetworkRecovery {
    public void attemptAutoRecovery(ValidationResult validationResult) {
        if (validationResult.isValid()) return;
        
        LOGGER.warn("Attempting automatic network recovery...");
        
        for (ValidationError error : validationResult.getErrors()) {
            switch (error.getType()) {
                case DATA_CORRUPTION:
                    fixDataCorruption(error);
                    break;
                case CONNECTION_ERROR:
                    rebuildConnections(error);
                    break;
                case NODE_FAILURE:
                    isolateFailedNode(error);
                    break;
            }
        }
        
        // 验证修复结果
        ValidationResult afterFix = validateNetwork();
        if (afterFix.isValid()) {
            LOGGER.info("Network recovery successful");
        } else {
            LOGGER.error("Network recovery failed, manual intervention required");
        }
    }
}
```

---
*相关文档链接：*
- [四维魔力系统详解](../../concepts/four-dimensional-mana.md)
- [网络同步机制](../../architecture/network-synchronization.md)
- [性能优化策略](../../development/performance-optimization.md)