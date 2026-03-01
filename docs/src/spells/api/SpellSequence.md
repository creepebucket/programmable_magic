# SpellSequence 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/spells/api/SpellSequence.java`  
**包名**: `org.creepebucket.programmable_magic.spells.api`  
**继承关系**: `Object` → `SpellSequence`  
**设计模式**: 双向链表容器

## 🎯 类设计目的

`SpellSequence` 是管理法术链表的**核心容器类**，专门用于组织、操作和维护法术节点序列。它是连接法术编译、执行和调试的关键数据结构。

## 🏗️ 数据结构实现

### 双向链表设计
```java
public class SpellSequence {
    // 链表边界指针
    public SpellItemLogic head;  // 指向链表第一个节点
    public SpellItemLogic tail;  // 指向链表最后一个节点
}
```

**设计优势对比**：
```
双向链表 vs ArrayList
✅ 动态插入/删除: O(1) vs ❌ O(n)
✅ 内存局部性: 好 vs ❌ 可能碎片化
✅ 前后遍历: 高效 vs ❌ 反向较慢
✅ 表达式解析: 天然适合 vs ❌ 不适合
```

### 空状态标准化约定
```java
// 空链表的标准表示
private boolean isEmpty() {
    return head == null && tail == null;  // 严格相等判断
}

// 约定：空表时 head==null 且 tail==null 且 size==0
// 非空时 head/tail 始终指向有效的首尾节点
```

## 🔧 核心操作方法详解

### 基础操作方法

#### `pushLeft()` - 左侧插入操作
```java
public SpellSequence pushLeft(SpellItemLogic spell) {
    // 设置新节点的链表关系
    spell.prev = null;    // 新节点前驱置空（将成为新的头部）
    spell.next = head;    // 新节点后继指向原头部
    
    // 更新原头部节点
    if (head != null) {
        head.prev = spell; // 原头部前驱指向新节点
    }
    
    // 更新序列边界
    head = spell;         // 新节点成为新的头部
    
    // 处理空序列特殊情况
    if (tail == null) {
        tail = spell;     // 如果原序列为空，尾部也指向新节点
    }
    
    return this; // 支持链式调用
}
```

**使用场景示例**：
```java
// 构建数学表达式: 1 + 2 + 3
SpellSequence seq = new SpellSequence();
seq.pushLeft(new NumberDigitSpell(3));  // [3]
seq.pushLeft(new AdditionSpell());      // [+ 3]  
seq.pushLeft(new NumberDigitSpell(2));  // [2 + 3]
seq.pushLeft(new AdditionSpell());      // [+ 2 + 3]
seq.pushLeft(new NumberDigitSpell(1));  // [1 + 2 + 3]

// 最终序列: 1 → + → 2 → + → 3
```

#### `pushRight()` - 右侧插入操作
```java
public SpellSequence pushRight(SpellItemLogic spell) {
    // 设置新节点的链表关系
    spell.next = null;    // 新节点后继置空（将成为新的尾部）
    spell.prev = tail;    // 新节点前驱指向原尾部
    
    // 更新原尾部节点
    if (tail != null) {
        tail.next = spell; // 原尾部后继指向新节点
    }
    
    // 更新序列边界
    tail = spell;         // 新节点成为新的尾部
    
    // 处理空序列特殊情况
    if (head == null) {
        head = spell;     // 如果原序列为空，头部也指向新节点
    }
    
    return this; // 支持链式调用
}
```

**使用场景示例**：
```java
// 按执行顺序构建序列
SpellSequence seq = new SpellSequence();
seq.pushRight(new CastSpell());        // [Cast]
seq.pushRight(new WaitSpell(5));       // [Cast → Wait(5)]
seq.pushRight(new EffectSpell());      // [Cast → Wait(5) → Effect]

// 执行顺序就是添加顺序
```

#### `popLeft()` / `popRight()` - 弹出操作
```java
public SpellItemLogic popLeft() {
    // 空序列检查
    if (head == null) {
        throw new RuntimeException("试图对空法术列表进行popLeft操作");
    }
    
    // 保存要返回的节点
    SpellItemLogic originalHead = head;
    SpellItemLogic newHead = head.next;
    
    if (newHead == null) {
        // 特殊情况：链表只有一个元素
        head = null;
        tail = null;
    } else {
        // 一般情况：更新头部指针
        head = newHead;
        newHead.prev = null;  // 新头部前驱置空
    }
    
    // 清理弹出节点的链表引用
    originalHead.next = null;
    originalHead.prev = null;
    
    return originalHead;
}
```

### 高级操作方法

#### `pushLeft(SpellSequence)` - 批量左侧插入
```java
public void pushLeft(SpellSequence list) {
    // 空链表检查
    if (list.head == null) return; // 被插入链表为空，无需操作
    
    if (head == null) {
        // 当前链表为空，直接复制边界指针
        head = list.head;
        tail = list.tail;
        return;
    }
    
    // 连接两个链表
    list.tail.next = head;    // 被插入链表尾部连接到当前链表头部
    head.prev = list.tail;    // 当前链表头部前驱指向被插入链表尾部
    head = list.head;         // 更新当前链表头部为被插入链表头部
}
```

**实际应用示例**：
```java
// 编译器优化场景
SpellSequence mainSeq = new SpellSequence();
SpellSequence prefixSeq = new SpellSequence();

// 构建前缀序列
prefixSeq.pushRight(new SetupEnvironmentSpell());
prefixSeq.pushRight(new InitializeVariablesSpell());

// 构建主序列
mainSeq.pushRight(new MainLogicSpell());
mainSeq.pushRight(new CleanupSpell());

// 将前缀序列插入到主序列开头
mainSeq.pushLeft(prefixSeq);
// 结果: [SetupEnvironment → InitializeVariables → MainLogic → Cleanup]
```

#### `replaceSection()` - 子序列替换操作
```java
public void replaceSection(SpellItemLogic L, SpellItemLogic R, SpellSequence section) {
    // 获取边界节点的邻居
    SpellItemLogic prev = L.prev;  // L的前驱节点
    SpellItemLogic next = R.next;  // R的后继节点
    
    // 获取新序列的边界
    SpellItemLogic newHead = section.head;
    SpellItemLogic newTail = section.tail;
    
    if (newHead != null) {
        // section非空的情况 - 执行替换
        newHead.prev = prev;           // 新序列头部前驱连接
        if (prev != null) prev.next = newHead;
        if (head == L) head = newHead; // 更新序列头部（如果需要）
        
        newTail.next = next;           // 新序列尾部后继连接
        if (next != null) next.prev = newTail;
        if (tail == R) tail = newTail; // 更新序列尾部（如果需要）
    } else {
        // section为空的情况 - 相当于删除[L..R]区间
        if (prev != null) prev.next = next;
        if (next != null) next.prev = prev;
        if (head == L) head = next;    // 更新头部
        if (tail == R) tail = prev;    // 更新尾部
    }
}
```

**编译优化示例**：
```java
// 常量折叠优化
SpellItemLogic startOpt = findConstantExpressionStart(sequence);
SpellItemLogic endOpt = findConstantExpressionEnd(sequence);

// 执行优化计算
SpellSequence optimized = performConstantFolding(startOpt, endOpt);
sequence.replaceSection(startOpt, endOpt, optimized);
```

#### `subSequence()` - 子序列提取操作
```java
public SpellSequence subSequence(SpellItemLogic L, SpellItemLogic R) {
    SpellSequence seq = new SpellSequence();
    
    SpellItemLogic p = L;  // 从左边界开始
    while (true) {
        // 深拷贝节点（重要：避免影响原序列）
        seq.pushRight(p.clone());
        
        if (p == R) break;     // 到达右边界，结束
        
        p = p.next;            // 移动到下一个节点
        
        if (p == null) {
            throw new RuntimeException("在对SpellSequence进行subSequence操作时, LR不连通");
        }
    }
    
    return seq;
}
```

## 🔄 与调度场算法的协作

### 表达式构建过程详解
```java
// 中缀表达式: 1 + 2 * 3
// 目标后缀: 1 2 3 * +

public SpellSequence convertToPostfix(String infixExpression) {
    SpellSequence result = new SpellSequence();
    Stack<SpellItemLogic> operatorStack = new Stack<>();
    
    // 词法分析和语法分析过程
    for (Token token : tokenize(infixExpression)) {
        if (token.isNumber()) {
            // 数字直接加入结果序列
            result.pushRight(new NumberDigitSpell(token.getValue()));
        } else if (token.isOperator()) {
            // 操作符处理
            SpellItemLogic currentOp = createOperatorSpell(token);
            
            // 调度场算法核心逻辑
            while (!operatorStack.isEmpty() && 
                   shouldPopOperator(operatorStack.peek(), currentOp)) {
                result.pushRight(operatorStack.pop());
            }
            operatorStack.push(currentOp);
        }
    }
    
    // 处理剩余操作符
    while (!operatorStack.isEmpty()) {
        result.pushRight(operatorStack.pop());
    }
    
    return result;
}
```

## 🎯 实际应用案例

### 1. 法术编译器中的使用
```java
public class SpellCompiler {
    public SpellSequence compile(Container spellContainer) {
        SpellSequence sequence = new SpellSequence();
        
        // 从容器中提取法术并构建序列
        for (int i = 0; i < spellContainer.getContainerSize(); i++) {
            ItemStack stack = spellContainer.getItem(i);
            if (stack.getItem() instanceof BaseSpellItem spellItem) {
                SpellItemLogic spellLogic = spellItem.getLogic().clone();
                sequence.pushRight(spellLogic);
            }
        }
        
        // 执行调度场算法转换
        return shuntingYardConvert(sequence);
    }
}
```

### 2. 调试器中的序列操作
```java
public class SpellDebugger {
    public void stepThroughSequence(SpellSequence sequence) {
        // 提取当前执行点附近的子序列用于调试显示
        SpellItemLogic current = findCurrentSpell(sequence);
        SpellItemLogic contextStart = findContextStart(current, 3);
        SpellItemLogic contextEnd = findContextEnd(current, 3);
        
        SpellSequence context = sequence.subSequence(contextStart, contextEnd);
        
        // 在调试界面显示上下文
        displayDebugContext(context);
    }
}
```

### 3. 序列优化和重构
```java
public class SequenceOptimizer {
    public SpellSequence optimize(SpellSequence original) {
        SpellSequence optimized = new SpellSequence();
        SpellItemLogic current = original.head;
        
        while (current != null) {
            if (canOptimize(current)) {
                // 找到可优化的子序列
                SpellItemLogic end = findOptimizationBoundary(current);
                SpellSequence optimizedSub = optimizeSubsequence(
                    original.subSequence(current, end)
                );
                
                // 替换原序列中的子序列
                optimized.pushRight(optimizedSub);
                current = end.next;
            } else {
                // 无法优化，直接复制
                optimized.pushRight(current.clone());
                current = current.next;
            }
        }
        
        return optimized;
    }
}
```

## ⚡ 性能特性和优化

### 内存管理优化
```java
public class SpellSequencePool {
    private static final Queue<SpellSequence> pool = new ConcurrentLinkedQueue<>();
    
    public static SpellSequence acquire() {
        SpellSequence seq = pool.poll();
        return seq != null ? seq : new SpellSequence();
    }
    
    public static void release(SpellSequence sequence) {
        sequence.clear(); // 清理节点引用
        pool.offer(sequence);
    }
    
    private void clear() {
        // 清理所有节点引用，避免内存泄漏
        SpellItemLogic current = head;
        while (current != null) {
            SpellItemLogic next = current.next;
            current.next = null;
            current.prev = null;
            current = next;
        }
        head = null;
        tail = null;
    }
}
```

### 批量操作优化
```java
public class BatchOperations {
    // 一次性插入多个节点
    public void insertBatch(SpellItemLogic afterNode, List<SpellItemLogic> nodes) {
        if (nodes.isEmpty()) return;
        
        // 构建新节点链表
        SpellItemLogic batchHead = nodes.get(0);
        SpellItemLogic batchTail = nodes.get(nodes.size() - 1);
        
        // 连接前后关系
        SpellItemLogic next = afterNode.next;
        afterNode.next = batchHead;
        batchHead.prev = afterNode;
        
        if (next != null) {
            next.prev = batchTail;
        }
        batchTail.next = next;
        
        // 更新序列边界
        if (tail == afterNode) {
            tail = batchTail;
        }
    }
}
```

## 🛡️ 错误处理和边界情况

### 完整的异常处理
```java
public class SequenceValidator {
    public static void validate(SpellSequence sequence) {
        // 空序列检查
        if (sequence.head == null && sequence.tail != null) {
            throw new IllegalStateException("序列头部为空但尾部非空");
        }
        
        if (sequence.head != null && sequence.tail == null) {
            throw new IllegalStateException("序列尾部为空但头部非空");
        }
        
        // 连通性检查
        if (sequence.head != null) {
            checkForwardConnectivity(sequence.head);
            checkBackwardConnectivity(sequence.tail);
        }
    }
    
    private static void checkForwardConnectivity(SpellItemLogic start) {
        SpellItemLogic current = start;
        while (current.next != null) {
            if (current.next.prev != current) {
                throw new IllegalStateException("前向连接不一致");
            }
            current = current.next;
        }
    }
    
    private static void checkBackwardConnectivity(SpellItemLogic end) {
        SpellItemLogic current = end;
        while (current.prev != null) {
            if (current.prev.next != current) {
                throw new IllegalStateException("后向连接不一致");
            }
            current = current.prev;
        }
    }
}
```

## 🔗 与其他核心类的关系

### 与 SpellItemLogic 的关系
```java
// SpellSequence 管理 SpellItemLogic 节点
SpellSequence sequence = new SpellSequence();
sequence.pushRight(new AdditionSpell());     // 添加节点
sequence.pushRight(new NumberDigitSpell(5)); // 添加节点

// 节点通过 next/prev 指针形成链式结构
AdditionSpell.next = NumberDigitSpell(5)
NumberDigitSpell(5).prev = AdditionSpell
```

### 与 SpellCompiler 的协作
```java
// 编译流程中的数据流
Container spellItems → SpellSequence (中缀) → SpellSequence (后缀) → SpellEntity
```

### 与 SpellEntity 的集成
```java
// SpellEntity 持有并执行 SpellSequence
SpellEntity entity = new SpellEntity(
    level, player, spellSequence, spellData, mana, plugins, debugMode
);
// entity.tick() 中遍历执行序列中的法术
```

---
*相关文档链接：*
- [SpellItemLogic 详解](SpellItemLogic.md)
- [ExecutionResult 详解](ExecutionResult.md)
- [SpellCompiler 详解](../SpellCompiler.md)
- [调度场算法详解](../../../concepts/shunting-yard-algorithm.md)
