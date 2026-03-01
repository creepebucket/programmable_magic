# SpellCompiler 类详解

## 📋 类基本信息

**文件路径**: `src/main/java/org/creepebucket/programmable_magic/spells/SpellCompiler.java`  
**包名**: `org.creepebucket.programmable_magic.spells`  
**设计模式**: 编译器模式 + 策略模式

## 🎯 类设计目的

`SpellCompiler` 是法术系统的**核心编译器**，负责将玩家构建的法术序列转换为可执行的后缀表达式形式。它实现了调度场算法（Shunting Yard Algorithm）来处理法术的优先级和结合性。

## 🏗️ 核心数据结构

### 编译器状态管理
```java
public class SpellCompiler {
    // 编译错误收集
    private final List<CompilationError> errors = new ArrayList<>();
    private final List<CompilationWarning> warnings = new ArrayList<>();
    
    // 编译选项和配置
    private CompilerOptions options;
    private OptimizationLevel optimizationLevel;
    
    // 缓存机制
    private final Map<String, CompiledSpell> compilationCache = new ConcurrentHashMap<>();
    private final LruCache<Container, SpellSequence> containerCache = 
        new LruCache<>(100); // 最近最少使用缓存
}
```

### 编译配置选项
```java
public static class CompilerOptions {
    private boolean enableOptimization = true;        // 启用优化
    private boolean strictTypeChecking = true;        // 严格类型检查
    private boolean allowRuntimeEvaluation = false;   // 允许运行时求值
    private int maxRecursionDepth = 50;               // 最大递归深度
    private boolean debugMode = false;                // 调试模式
    private boolean generateDebugInfo = false;        // 生成调试信息
}
```

## 🔧 核心方法详解

### 主编译流程

#### `compile()` - 主编译入口
```java
public CompilationResult compile(Container spellContainer) {
    // 清理上次编译状态
    clearCompilationState();
    
    try {
        // 1. 预处理阶段
        PreprocessingResult preprocessed = preprocess(spellContainer);
        if (!preprocessed.isValid()) {
            return CompilationResult.failure(errors);
        }
        
        // 2. 词法分析阶段
        List<Token> tokens = tokenize(preprocessed.getSpellItems());
        if (tokens.isEmpty()) {
            addError("Empty spell sequence");
            return CompilationResult.failure(errors);
        }
        
        // 3. 语法分析和调度场算法
        SpellSequence compiledSequence = shuntingYardAlgorithm(tokens);
        if (compiledSequence == null) {
            return CompilationResult.failure(errors);
        }
        
        // 4. 语义分析阶段
        if (!semanticAnalysis(compiledSequence)) {
            return CompilationResult.failure(errors);
        }
        
        // 5. 优化阶段
        if (options.enableOptimization) {
            compiledSequence = optimize(compiledSequence);
        }
        
        // 6. 生成最终结果
        CompiledSpell compiledSpell = new CompiledSpell(
            compiledSequence, 
            generateMetadata(tokens),
            System.currentTimeMillis()
        );
        
        // 缓存编译结果
        cacheCompilation(spellContainer, compiledSpell);
        
        return CompilationResult.success(compiledSpell, warnings);
        
    } catch (Exception e) {
        addError("Compilation failed: " + e.getMessage());
        LOGGER.error("Spell compilation error", e);
        return CompilationResult.failure(errors);
    }
}
```

### 调度场算法实现

#### `shuntingYardAlgorithm()` - 核心算法
```java
private SpellSequence shuntingYardAlgorithm(List<Token> tokens) {
    SpellSequence outputQueue = new SpellSequence();
    Deque<SpellItemLogic> operatorStack = new ArrayDeque<>();
    
    for (Token token : tokens) {
        switch (token.getType()) {
            case SPELL:
                // 法术直接输出到队列
                outputQueue.pushRight(token.getSpellLogic().clone());
                break;
                
            case OPERATOR:
                // 操作符处理
                SpellItemLogic currentOperator = token.getSpellLogic();
                
                // 根据优先级和结合性决定是否弹出操作符
                while (!operatorStack.isEmpty()) {
                    SpellItemLogic topOperator = operatorStack.peek();
                    
                    if (shouldPopOperator(topOperator, currentOperator)) {
                        outputQueue.pushRight(operatorStack.pop());
                    } else {
                        break;
                    }
                }
                
                operatorStack.push(currentOperator);
                break;
                
            case LEFT_PARENTHESIS:
                // 左括号压入栈
                operatorStack.push(token.getSpellLogic());
                break;
                
            case RIGHT_PARENTHESIS:
                // 右括号处理
                boolean matched = false;
                while (!operatorStack.isEmpty()) {
                    SpellItemLogic operator = operatorStack.pop();
                    
                    if (isLeftParenthesis(operator)) {
                        matched = true;
                        break;
                    }
                    
                    outputQueue.pushRight(operator);
                }
                
                if (!matched) {
                    addError("Mismatched parentheses at position " + token.getPosition());
                    return null;
                }
                break;
        }
    }
    
    // 处理剩余的操作符
    while (!operatorStack.isEmpty()) {
        SpellItemLogic operator = operatorStack.pop();
        
        if (isParenthesis(operator)) {
            addError("Mismatched parentheses");
            return null;
        }
        
        outputQueue.pushRight(operator);
    }
    
    return outputQueue;
}

private boolean shouldPopOperator(SpellItemLogic stackOp, SpellItemLogic currentOp) {
    // 特殊标记处理
    if (currentOp.bypassShunting || stackOp.bypassShunting) {
        return false;
    }
    
    // 优先级比较
    if (stackOp.precedence > currentOp.precedence) {
        return true;
    }
    
    if (stackOp.precedence == currentOp.precedence) {
        // 相同优先级时根据结合性决定
        return !currentOp.rightConnectivity;
    }
    
    return false;
}
```

### 预处理阶段

#### `preprocess()` - 输入预处理
```java
private PreprocessingResult preprocess(Container spellContainer) {
    List<ItemStack> validSpells = new ArrayList<>();
    List<PreprocessingWarning> warnings = new ArrayList<>();
    
    // 验证容器内容
    for (int i = 0; i < spellContainer.getContainerSize(); i++) {
        ItemStack stack = spellContainer.getItem(i);
        
        if (stack.isEmpty()) {
            continue; // 跳过空槽位
        }
        
        if (!(stack.getItem() instanceof SpellItem)) {
            addWarning("Non-spell item found at slot " + i);
            continue;
        }
        
        SpellItem spellItem = (SpellItem) stack.getItem();
        SpellItemLogic spellLogic = spellItem.getLogic();
        
        // 验证法术可用性
        if (!isSpellAvailable(spellLogic)) {
            addWarning("Unavailable spell '" + spellLogic.name + "' at slot " + i);
            continue;
        }
        
        validSpells.add(stack);
    }
    
    // 检查最小长度
    if (validSpells.size() < 1) {
        addError("Spell sequence too short");
        return PreprocessingResult.invalid(errors);
    }
    
    return new PreprocessingResult(validSpells, warnings);
}
```

### 语义分析

#### `semanticAnalysis()` - 语义验证
```java
private boolean semanticAnalysis(SpellSequence sequence) {
    List<SpellItemLogic> spellList = sequence.toList();
    
    // 1. 类型检查
    if (!performTypeChecking(spellList)) {
        return false;
    }
    
    // 2. 参数数量检查
    if (!validateParameterCounts(spellList)) {
        return false;
    }
    
    // 3. 循环引用检查
    if (hasCircularDependencies(spellList)) {
        addError("Circular dependency detected in spell sequence");
        return false;
    }
    
    // 4. 配对标记检查
    if (!validatePairedSpells(spellList)) {
        return false;
    }
    
    return true;
}

private boolean performTypeChecking(List<SpellItemLogic> spells) {
    Stack<List<SpellValueType>> typeStack = new Stack<>();
    
    for (SpellItemLogic spell : spells) {
        // 检查输入参数类型匹配
        if (!checkInputTypes(spell, typeStack)) {
            return false;
        }
        
        // 推入输出类型
        for (List<SpellValueType> outputSignature : spell.outputTypes) {
            typeStack.push(outputSignature);
        }
    }
    
    return true;
}
```

## ⚡ 优化系统

### 常量折叠优化
```java
private SpellSequence optimize(SpellSequence original) {
    SpellSequence optimized = original.clone();
    
    switch (optimizationLevel) {
        case BASIC:
            optimized = performBasicOptimizations(optimized);
            break;
        case ADVANCED:
            optimized = performAdvancedOptimizations(optimized);
            break;
        case AGGRESSIVE:
            optimized = performAggressiveOptimizations(optimized);
            break;
    }
    
    return optimized;
}

private SpellSequence performBasicOptimizations(SpellSequence sequence) {
    SpellSequence result = new SpellSequence();
    List<SpellItemLogic> spells = sequence.toList();
    
    for (int i = 0; i < spells.size(); i++) {
        SpellItemLogic current = spells.get(i);
        
        // 常量折叠优化
        if (canFoldConstants(spells, i)) {
            SpellItemLogic folded = foldConstants(spells, i);
            result.pushRight(folded);
            i += getFoldedSpellsCount(spells, i) - 1; // 跳过已折叠的法术
        } else {
            result.pushRight(current.clone());
        }
    }
    
    return result;
}

private boolean canFoldConstants(List<SpellItemLogic> spells, int index) {
    if (index >= spells.size() - 2) return false;
    
    SpellItemLogic op = spells.get(index + 1);
    SpellItemLogic operand1 = spells.get(index);
    SpellItemLogic operand2 = spells.get(index + 2);
    
    // 检查是否都是常量字面量法术
    return isLiteralSpell(operand1) && isLiteralSpell(operand2) && 
           isComputableOperator(op);
}

private SpellItemLogic foldConstants(List<SpellItemLogic> spells, int index) {
    SpellItemLogic operand1 = spells.get(index);
    SpellItemLogic op = spells.get(index + 1);
    SpellItemLogic operand2 = spells.get(index + 2);
    
    // 执行编译时常量计算
    Object result = executeConstantOperation(operand1, op, operand2);
    
    // 创建结果字面量法术
    return createLiteralSpell(result);
}
```

### 死代码消除
```java
private SpellSequence eliminateDeadCode(SpellSequence sequence) {
    List<SpellItemLogic> reachableSpells = new ArrayList<>();
    Set<SpellItemLogic> visited = new HashSet<>();
    
    // 从序列头开始的可达性分析
    analyzeReachability(sequence.head, reachableSpells, visited);
    
    // 构建优化后的序列
    SpellSequence optimized = new SpellSequence();
    for (SpellItemLogic spell : reachableSpells) {
        optimized.pushRight(spell.clone());
    }
    
    return optimized;
}

private void analyzeReachability(SpellItemLogic spell, 
                               List<SpellItemLogic> reachable,
                               Set<SpellItemLogic> visited) {
    if (spell == null || visited.contains(spell)) {
        return;
    }
    
    visited.add(spell);
    reachable.add(spell);
    
    // 分析控制流转移
    if (spell instanceof SpellItemLogic.ControlMod) {
        // 处理分支和跳转
        analyzeControlFlow(spell, reachable, visited);
    }
    
    // 继续分析下一法术
    if (spell.next != null) {
        analyzeReachability(spell.next, reachable, visited);
    }
}
```

## 🛡️ 错误处理系统

### 编译错误类型定义
```java
public enum CompilationErrorType {
    SYNTAX_ERROR,           // 语法错误
    TYPE_MISMATCH,          // 类型不匹配
    PARAMETER_COUNT,        // 参数数量错误
    CIRCULAR_DEPENDENCY,    // 循环依赖
    INVALID_SPELL,          // 无效法术
    COMPILATION_TIMEOUT,    // 编译超时
    MEMORY_LIMIT_EXCEEDED   // 内存限制超出
}

public class CompilationError {
    private final CompilationErrorType type;
    private final String message;
    private final int position;
    private final Throwable cause;
    
    public CompilationError(CompilationErrorType type, String message, int position) {
        this.type = type;
        this.message = message;
        this.position = position;
        this.cause = null;
    }
}
```

### 错误恢复机制
```java
public class ErrorRecovery {
    public RecoveryStrategy suggestRecovery(CompilationError error) {
        switch (error.getType()) {
            case SYNTAX_ERROR:
                return RecoveryStrategy.SKIP_TOKEN;
            case TYPE_MISMATCH:
                return RecoveryStrategy.INSERT_CAST;
            case PARAMETER_COUNT:
                return RecoveryStrategy.ADD_DEFAULT_PARAMS;
            case CIRCULAR_DEPENDENCY:
                return RecoveryStrategy.BREAK_CYCLE;
            default:
                return RecoveryStrategy.ABORT_COMPILATION;
        }
    }
    
    public SpellSequence applyRecovery(RecoveryStrategy strategy, 
                                     SpellSequence sequence,
                                     CompilationError error) {
        switch (strategy) {
            case SKIP_TOKEN:
                return removeErrorToken(sequence, error);
            case INSERT_CAST:
                return insertTypeConversion(sequence, error);
            case ADD_DEFAULT_PARAMS:
                return addMissingParameters(sequence, error);
            case BREAK_CYCLE:
                return breakDependencyCycle(sequence, error);
            default:
                return sequence; // 无法恢复
        }
    }
}
```

## 🔧 实际使用示例

### 基本编译使用
```java
public class SpellCompilationExample {
    public static void basicCompilation() {
        // 创建编译器实例
        SpellCompiler compiler = new SpellCompiler();
        
        // 配置编译选项
        CompilerOptions options = new CompilerOptions();
        options.setEnableOptimization(true);
        options.setStrictTypeChecking(true);
        compiler.setOptions(options);
        
        // 准备法术容器
        SimpleContainer spellContainer = new SimpleContainer(9);
        spellContainer.setItem(0, new ItemStack(ModItems.NUMBER_DIGIT_SPELL.get()));
        spellContainer.setItem(1, new ItemStack(ModItems.ADDITION_SPELL.get()));
        spellContainer.setItem(2, new ItemStack(ModItems.NUMBER_DIGIT_SPELL.get()));
        
        // 执行编译
        CompilationResult result = compiler.compile(spellContainer);
        
        if (result.isSuccess()) {
            SpellSequence executableSequence = result.getCompiledSpell().getSequence();
            System.out.println("Compilation successful!");
            System.out.println("Compiled sequence length: " + executableSequence.size());
        } else {
            System.out.println("Compilation failed:");
            result.getErrors().forEach(error -> 
                System.out.println("- " + error.getMessage())
            );
        }
    }
}
```

### 高级编译配置
```java
public class AdvancedCompilation {
    public static CompilationResult compileWithCustomSettings(Container container) {
        SpellCompiler compiler = new SpellCompiler();
        
        // 高级配置
        CompilerOptions options = new CompilerOptions();
        options.setOptimizationLevel(OptimizationLevel.AGGRESSIVE);
        options.setDebugMode(true);
        options.setGenerateDebugInfo(true);
        options.setMaxRecursionDepth(100);
        
        compiler.setOptions(options);
        
        // 添加自定义优化器
        compiler.addOptimizer(new CustomOptimizer());
        
        // 添加编译监听器
        compiler.addCompilationListener(new CompilationListener() {
            @Override
            public void onCompilationStart(Container container) {
                System.out.println("Starting compilation...");
            }
            
            @Override
            public void onCompilationComplete(CompilationResult result) {
                System.out.println("Compilation completed with " + 
                    result.getWarnings().size() + " warnings");
            }
        });
        
        return compiler.compile(container);
    }
}
```

### 批量编译处理
```java
public class BatchCompiler {
    private final SpellCompiler compiler = new SpellCompiler();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    public List<CompilationResult> compileBatch(List<Container> containers) {
        List<CompletableFuture<CompilationResult>> futures = new ArrayList<>();
        
        for (Container container : containers) {
            CompletableFuture<CompilationResult> future = 
                CompletableFuture.supplyAsync(() -> compiler.compile(container), executor);
            futures.add(future);
        }
        
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
    
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

---
*相关文档链接：*
- [调度场算法详解](../../concepts/shunting-yard-algorithm.md)
- [SpellSequence 详解](api/SpellSequence.md)
- [编译器优化技术](../../development/compiler-optimization.md)