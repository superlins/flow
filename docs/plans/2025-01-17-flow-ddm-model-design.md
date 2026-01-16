# Flow - 轻量级DAG工作流编排引擎 DDD领域模型设计

> **日期**: 2025-01-17
> **版本**: 1.0
> **目标**: 基于DDD原则重新设计和优化Flow项目的领域模型，支持轻量级DAG工作流编排

---

## 目录

1. [项目概述](#1-项目概述)
2. [领域边界与限界上下文](#2-领域边界与限界上下文)
3. [核心聚合根设计](#3-核心聚合根设计)
4. [实体与值对象](#4-实体与值对象)
5. [领域服务设计](#5-领域服务设计)
6. [执行模型与上下文](#6-执行模型与上下文)
7. [现有模型优化建议](#7-现有模型优化建议)
8. [业务规则与不变量](#8-业务规则与不变量)
9. [存储策略](#9-存储策略)

---

## 1. 项目概述

### 1.1 项目目标

Flow是一个**轻量级DAG工作流编排引擎**，支持多个数据源的声明式编排组合。

**核心能力：**
- 声明式定义工作流（DAG有向无环图）
- 多数据源节点串行/并行执行（基于连接的隐式并行）
- 条件分支、循环迭代等流控制
- 数据转换和映射
- 完整的执行历史追踪

**参考设计：**
- n8n 的节点和连接模型
- Flowable/Camunda 的节点继承架构

### 1.2 技术要求

- **语言**: Java 25
- **框架**: Spring Boot 4.0.1, Spring WebFlux
- **数据库**: PostgreSQL (R2DBC)
- **验证**: NetworkNT JSON Schema Validator
- **插件**: PF4J (用于扩展)

### 1.3 核心聚合根

| 聚合根 | 标识版本 | 职责 |
|--------|----------|------|
| **ApiDatasource** | ✅ (key, version) | 底层数据源能力定义 |
| **ApiService** | ❌ (serviceId) | 业务API统一视图 |
| **Workflow** | ✅ (key, version) | 工作流编排定义 |
| **WorkflowExecution** | ❌ (executionId) | 执行历史记录 |

---

## 2. 领域边界与限界上下文

### 2.1 上下文映射

```
┌─────────────────────────────────────────────┐
│     Workflow Orchestration Context         │
│  (核心上下文 - 本设计文档范围)              │
│                                             │
│  • Workflow聚合根                           │
│  • WorkflowExecution聚合根                  │
│  • 执行引擎和上下文模型                     │
└─────────────────────────────────────────────┘
              ↓ 引用
┌─────────────────────────────────────────────┐
│      Datasource Definition Context          │
│  (已存在 - 需优化)                          │
│                                             │
│  • ApiDatasource聚合根                     │
│  • Connector框架                            │
└─────────────────────────────────────────────┘
              ↓ 引用
┌─────────────────────────────────────────────┐
│      API Publication Context                 │
│  (已存在 - 需重构)                          │
│                                             │
│  • ApiService聚合根                         │
│  • REST API层                               │
└─────────────────────────────────────────────┘
```

### 2.2 聚合根关系

```
ApiService (聚合根)
  ├── mode: DATASOURCE | WORKFLOW
  └── binding: ServiceBinding
         ├── [DATASOURCE] datasourceId: DatasourceId
         └── [WORKFLOW]    workflowId: WorkflowId, workflowVersion: Integer

ApiDatasource (聚合根)
  └── 零到多个 ApiService 引用（通过DS-1规则保护）

Workflow (聚合根)
  ├── graph: WorkflowGraph (DAG)
  │   ├── nodes: Map<String, WorkflowNode>
  │   └── connections: List<WorkflowConnection>
  ├── config: WorkflowConfiguration
  └── variables: List<WorkflowVariable>
       └── 零到多个 ApiService 引用（服务引用）

WorkflowExecution (聚合根)
  ├── workflowId: WorkflowId (弱引用)
  ├── status: ExecutionStatus
  └── nodeExecutions: List<NodeExecution> (实体)
```

---

## 3. 核心聚合根设计

### 3.1 Workflow 聚合根

**职责**: 工作流编排定义的完整生命周期管理

#### 标识

```java
public final class WorkflowId implements ValueObject<WorkflowId> {
    private final String key;       // 业务可读标识
    private final Integer version;  // 版本号（从1开始递增）

    // sameValueAs, equals, hashCode, toString...
}
```

#### 聚合根

```java
public final class Workflow implements DomainEntity<Workflow> {

    // === 标识 ===
    private final WorkflowId id;  // (key, version)

    // === 状态 ===
    private WorkflowStatus status;  // DRAFT, ACTIVE, ARCHIVED

    // === 基础信息 ===
    private String name;
    private String description;

    // === 核心结构 ===
    private WorkflowGraph graph;           // DAG图结构
    private WorkflowConfiguration config;  // 执行配置（超时、重试等）
    private List<WorkflowVariable> variables;  // 变量定义

    // === 元数据 ===
    private final List<Object> domainEvents;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;

    // === 工厂方法 ===
    public static Workflow create(WorkflowId id, String name);

    public static Workflow restore(
            WorkflowId id, String name, String description,
            WorkflowStatus status, WorkflowGraph graph,
            WorkflowConfiguration config, List<WorkflowVariable> variables,
            Instant createdAt, Instant updatedAt, String createdBy);

    // === 领域行为 ===

    public void addNode(WorkflowNode node);
    public void removeNode(String nodeId);
    public void addConnection(WorkflowConnection connection);
    public void removeConnection(WorkflowConnection connection);
    public void activate();  // 激活工作流（验证DAG有效性）
    public void archive();   // 归档工作流
    public void addVariable(WorkflowVariable variable);

    // === Getters ===
    public WorkflowId id();
    public WorkflowStatus status();
    public String name();
    public WorkflowGraph graph();
    public List<WorkflowVariable> variables();
    public List<Object> pullDomainEvents();
}
```

#### 生命周期

```
DRAFT → ACTIVE → ARCHIVED
  ↑         ↓
  └─────────┘（归档后不能再回到DRAFT）

状态转换规则：
- DRAFT: 可以编辑结构（节点、连接、变量）
- ACTIVE: 不能编辑，可以被Service引用执行
- ARCHIVED: 不能执行，不能被新的Service引用，已存在的Service会报错
```

---

### 3.2 ApiService 聚合根（重构）

**职责**: 业务API的统一视图，支持两种运行模式

#### 标识

```java
public final class ServiceId implements ValueObject<ServiceId> {
    private final String value;  // serviceId，无版本

    // sameValueAs, equals, hashCode...
}
```

#### 聚合根

```java
public final class ApiService implements DomainEntity<ApiService> {

    private final ServiceId id;
    private ServiceStatus status;  // DISABLED, ENABLED

    // 基础信息
    private String name;
    private String description;

    // 核心属性
    private ServiceContract contract;    // 服务契约（输入输出schema）
    private ServiceBinding binding;      // 绑定对象（通用）

    // 元数据
    private final List<Object> domainEvents;
    private Instant createdAt;
    private Instant updatedAt;

    // === 工厂方法 ===
    public static ApiService create(
            ServiceId id, String name, String description,
            ServiceContract contract, ServiceBinding binding);

    public static ApiService restore(
            ServiceId id, String name, String description,
            ServiceStatus status, ServiceContract contract,
            ServiceBinding binding, Instant createdAt, Instant updatedAt);

    // === 领域行为 ===
    public void enable();
    public void disable();
    public void updateBinding(ServiceBinding newBinding);
    public void updateContract(ServiceContract newContract);

    // === Getters ===
    public ServiceId id();
    public ServiceStatus status();
    public ServiceContract contract();
    public ServiceBinding binding();
}
```

#### ServiceBinding 通用绑定对象

```java
public final class ServiceBinding implements ValueObject<ServiceBinding> {

    private final ServiceMode mode;  // DATASOURCE | WORKFLOW

    // Datasource模式时的引用
    private final DatasourceId datasourceId;
    private final Integer datasourceVersion;

    // Workflow模式时的引用
    private final WorkflowId workflowId;
    private final Integer workflowVersion;

    // 映射规则（通用）
    private final Map<String, FieldBinding> inputMapping;   // #serviceInput -> target
    private final Map<String, FieldBinding> outputMapping;  // target -> #serviceOutput

    public enum ServiceMode {
        DATASOURCE,  // 直接调用单个Datasource
        WORKFLOW     // 执行Workflow编排
    }

    // 使用示例：
    // DATASOURCE模式
    ServiceBinding.datasourceBuilder()
        .datasourceId(new DatasourceId("create-order", 1))
        .inputMapping(Map.of("userId", FieldBinding.of("{{ #serviceInput.clientUserId }}")))
        .outputMapping(Map.of("bizOrderId", FieldBinding.of("{{ #targetOutput.orderId }}")))
        .build();

    // WORKFLOW模式
    ServiceBinding.workflowBuilder()
        .workflowId(new WorkflowId("process-order", 2))
        .inputMapping(Map.of("orderContext", FieldBinding.of("{{ #serviceInput }}")))
        .outputMapping(Map.of("result", FieldBinding.of("{{ #targetOutput.finalResult }}")))
        .build();
}
```

---

### 3.3 ApiDatasource 聚合根（优化）

**职责**: 底层数据源能力定义

**现有设计保持不变**, 需优化的点见[第7节 - 现有模型优化建议](#7-现有模型优化建议)

```java
public final class ApiDatasource implements DomainEntity<ApiDatasource> {
    // 现有实现保持，不再重复
}
```

---

### 3.4 WorkflowExecution 聚合根

**职责**: 工作流执行历史记录

#### 标识

```java
public record ExecutionId(String value) implements ValueObject<ExecutionId> {
    public static ExecutionId generate() {
        return new ExecutionId(UUID.randomUUID().toString());
    }
}
```

#### 聚合根

```java
public final class WorkflowExecution implements DomainEntity<WorkflowExecution> {

    private final ExecutionId id;
    private WorkflowId workflowId;  // 弱引用（不保证引用存在）
    private ExecutionStatus status;

    // 输入输出
    private JsonNode input;
    private JsonNode output;

    // 执行信息
    private Instant startedAt;
    private Instant finishedAt;
    private Duration duration;
    private String error;

    // 节点执行历史（实体）
    private final List<NodeExecution> nodeExecutions;

    // 上下文快照
    private JsonNode contextSnapshot;

    // 元数据
    private final Map<String, String> metadata;

    public enum ExecutionStatus {
        RUNNING, COMPLETED, FAILED, CANCELLED, TIMEOUT
    }

    // === 工厂方法 ===
    public static WorkflowExecution create(WorkflowId workflowId, JsonNode input);

    // === 领域行为 ===
    public void markNodeStarted(String nodeId);
    public void markNodeCompleted(String nodeId, JsonNode output, Duration duration);
    public void markNodeFailed(String nodeId, String error);
    public void markSuccess(JsonNode output);
    public void markFailure(String error);
    public void markCancelled(String reason);
    public void saveContextSnapshot(ExecutionContext context);

    // === Getters ===
    public ExecutionId id();
    public ExecutionStatus status();
    public List<NodeExecution> nodeExecutions();
}
```

---

## 4. 实体与值对象

### 4.1 WorkflowGraph (值对象)

**职责**: DAG图结构定义

```java
public final class WorkflowGraph implements ValueObject<WorkflowGraph> {

    private final Map<String, WorkflowNode> nodes;
    private final List<WorkflowConnection> connections;

    // 预构建的拓扑关系（便于执行器使用）
    private transient Map<String, List<WorkflowConnection>> outboundConnections;
    private transient Map<String, List<WorkflowConnection>> inboundConnections;

    // === 验证方法 ===
    public boolean validateAsDAG();  // 验证无环
    public List<String> findStartNodes();  // 找起始节点（无入边）
    public List<String> findEndNodes();    // 找结束节点（无出边）

    // === 查询方法 ===
    public List<WorkflowConnection> getOutboundConnections(String nodeId);
    public List<WorkflowConnection> getInboundConnections(String nodeId);
    public WorkflowNode getNode(String nodeId);

    // === 构建器 ===
    public WorkflowGraph copyWith(WorkflowConnection connection);  // 验证用
}
```

---

### 4.2 WorkflowConnection (值对象)

**职责**: 节点间连接定义，表达数据流向

```java
public final class WorkflowConnection implements ValueObject<WorkflowConnection> {

    private final String sourceNodeId;
    private final String targetNodeId;
    private final String outputPort;   // 输出端口名
    private final String inputPort;    // 输入端口名
    private final MergeStrategy mergeStrategy;  // 扇入合并策略

    public enum MergeStrategy {
        ARRAY,          // 数组合并：[[res1], [res2], [res3]]
        FIRST_SUCCESS,  // 第一个成功的结果
        ALL_SUCCESS     // 所有结果放入同一个对象
    }

    // 示例并行连接：
    // NodeA -> NodeB (MergeStrategy.ARRAY)
    // NodeA -> NodeC (MergeStrategy.ARRAY)
    // NodeD <- NodeB (扇入自动合并 [NodeB输出, NodeC输出])
    // NodeD <- NodeC
}
```

---

### 4.3 NodeExecution (实体)

**职责**: 节点执行记录（是WorkflowExecution聚合的一部分）

```java
public final class NodeExecution {

    private final String nodeId;
    private NodeExecutionStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    private Duration duration;

    // 输入输出
    private JsonNode input;
    private JsonNode output;

    // 错误信息
    private String error;
    private String errorStack;

    // 重试信息
    private int attempt;
    private Integer maxAttempts;

    // === 工厂方法 ===
    public static NodeExecution started(String nodeId, Instant startedAt);
    public static NodeExecution restore(...);

    // === 领域行为 ===
    public void completed(JsonNode output, Duration duration);
    public void failed(String error);
    public void setRetryInfo(int attempt, Integer maxAttempts);
    public void incrementAttempt();

    // === Getters ===
    public String nodeId();
    public NodeExecutionStatus status();
    public int attempt();
}

public enum NodeExecutionStatus {
    RUNNING, COMPLETED, FAILED, RETRYING, SKIPPED
}
```

---

### 4.4 WorkflowConfiguration (值对象)

**职责**: 工作流执行配置

```java
public final class WorkflowConfiguration implements ValueObject<WorkflowConfiguration> {

    private final Duration timeout;           // 整个工作流超时
    private final RetryPolicy retryPolicy;    // 重试策略
    private final FailureStrategy failureStrategy;  // 失败策略
    private final boolean enableLogging;      // 是否启用详细日志
    private final boolean enableMetrics;      // 是否启用指标收集

    public enum FailureStrategy {
        STOP_ON_ERROR,      // 遇到错误立即停止
        CONTINUE_ON_ERROR,  // 跳过错误节点继续
        RESUME_ON_ERROR     // 在错误处暂停，可恢复
    }
}

public final class RetryPolicy implements ValueObject<RetryPolicy> {
    private final int maxAttempts;
    private final Duration backoff;
    private final double backoffMultiplier;
}
```

---

### 4.5 WorkflowVariable (值对象)

**职责**: 工作流级别变量定义

```java
public final class WorkflowVariable implements ValueObject<WorkflowVariable> {

    private final String name;
    private final String description;
    private final String defaultValue;  // SpEL表达式
    private final VariableScope scope;  // GLOBAL, LOCAL

    public enum VariableScope {
        GLOBAL,  // 工作流级别，所有节点共用
        LOCAL    // 节点级别，仅当前节点可见
    }

    /**
     * 初始化变量的值
     */
    public JsonNode initializeValue(ExecutionContext context);
}
```

---

### 4.6 ServiceContract (值对象 - 优化)

**职责**: ApiService的服务契约（输入输出schema）

**优化点**: 存储为字符串，不引入Jackson等第三方库

```java
public final class ServiceContract implements ValueObject<ServiceContract> {

    private final String inputSchema;   // JSON Schema字符串
    private final String outputSchema;  // JSON Schema字符串

    // 验证逻辑放在领域服务中，不在此值对象中
}
```

---

### 4.7 DatasourceContract (值对象 - 优化)

**职责**: ApiDatasource的契约（与DatasourceId一起版本化）

**优化点**: Contract不独立版本，生命周期完全依附于 ApiDatasource

```java
public final class DatasourceContract implements ValueObject<DatasourceContract> {

    private final String inputSchema;
    private final String outputSchema;
    private final boolean strict;  // true: 严格验证；false: 宽松验证

    // Contract随ApiDatasource版本化
    // 调用 updateContract() 需要创建新的ApiDatasource版本
}
```

---

## 5. 领域服务设计

### 5.1 WorkflowExecutionService

**职责**: 编排工作流执行

```java
@Service
public class WorkflowExecutionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;
    private final WorkflowDAGExecutor dagExecutor;
    private final DomainEventPublisher eventPublisher;

    /**
     * 执行工作流
     */
    public Mono<WorkflowExecution> execute(WorkflowId workflowId, JsonNode input) {
        return workflowRepository.findById(workflowId)
            .switchIfEmpty(Mono.error(new WorkflowNotFoundException(workflowId)))
            .flatMap(workflow -> {
                // 创建执行记录
                WorkflowExecution execution = WorkflowExecution.create(workflowId, input);

                // 保存并发布开始事件
                return executionRepository.save(execution)
                    .doOnNext(e -> pullAndPublishEvents(e))
                    // 执行DAG
                    .flatMap(e -> dagExecutor.execute(workflow.graph(), e))
                    // 保存执行结果
                    .flatMap(executionRepository::save)
                    .doOnNext(e -> pullAndPublishEvents(e));
            });
    }

    /**
     * 取消执行
     */
    public Mono<Void> cancel(ExecutionId executionId, String reason) {
        return executionRepository.findById(executionId)
            .switchIfEmpty(Mono.error(new ExecutionNotFoundException(executionId)))
            .flatMap(execution -> {
                if (execution.status() != ExecutionStatus.RUNNING) {
                    return Mono.error(new ExecutionNotRunningException(executionId));
                }
                execution.markCancelled(reason);
                return executionRepository.save(execution);
            });
    }

    private void pullAndPublishEvents(DomainEntity<?> entity) {
        entity.pullDomainEvents().forEach(eventPublisher::publish);
    }
}
```

---

### 5.2 ServiceExecutionRouter

**职责**: 路由ApiService执行（Datasource模式或Workflow模式）

```java
@Service
public class ServiceExecutionRouter {

    private final WorkflowExecutionService workflowExecutionService;
    private final DatasourceExecutionService datasourceExecutionService;

    /**
     * 路由执行
     */
    public Mono<JsonNode> execute(ApiService service, JsonNode input) {
        return switch (service.binding().mode()) {
            case DATASOURCE -> datasourceExecutionService.execute(
                service.binding().datasourceId(),
                service.binding().datasourceVersion(),
                input,
                service.binding().inputMapping(),
                service.binding().outputMapping()
            );

            case WORKFLOW -> workflowExecutionService.execute(
                service.binding().workflowId(),
                service.binding().workflowVersion(),
                input
            ).map(WorkflowExecution::output);
        };
    }
}
```

---

### 5.3 SchemaValidationService (优化)

**职责**: JSON Schema验证

**优化点**: 不在领域模型中引入Jackson

```java
@Service
public class SchemaValidationServiceImpl implements SchemaValidationService {

    private final ObjectMapper objectMapper;  // Jackson在这里，不在领域

    @Override
    public void validate(String schema, Object input) {
        SchemaValidatorsService validatorsService = SchemaValidatorsService.getInstance();
        JsonSchema jsonSchema = validatorsService.getSchema(schema);

        Set<ValidationMessage> errors = jsonSchema.validate(input);
        if (!errors.isEmpty()) {
            throw new SchemaValidationException(errors);
        }
    }
}
```

---

### 5.4 ExecutionAuditor (新增)

**职责**: 执行审计和统计

```java
@Service
public class ExecutionAuditor {

    private final WorkflowExecutionRepository executionRepository;
    private final NodeExecutionRepository nodeExecutionRepository;

    /**
     * 审计工作流执行
     */
    public Mono<ExecutionAudit> audit(ExecutionId executionId);

    /**
     * 查询失败的执行
     */
    public Flux<WorkflowExecution> findFailedExecutions(WorkflowId workflowId);

    /**
     * 统计执行成功率
     */
    public Mono<ExecutionStatistics> statistics(
        WorkflowId workflowId, Instant startTime, Instant endTime);
}
```

---

## 6. 执行模型与上下文

### 6.1 ExecutionContext (统一执行上下文)

**职责**: 单一上下文对象，支持单Datasource执行和Workflow执行

```java
public final class ExecutionContext {

    private final String executionId;
    private final Instant startedAt;

    // 输入输出
    private final JsonNode serviceInput;
    private JsonNode serviceOutput;

    // 执行上下文数据
    private final Map<String, JsonNode> variables;  // 工作流级别变量
    private final Map<String, JsonNode> context;     // 节点执行中间数据

    // 节点执行状态
    private final Map<String, NodeExecutionResult> nodeResults;

    // 配置与元数据
    private final ExecutionConfig config;
    private final Map<String, String> metadata;

    public record ExecutionContext(
        String executionId, Instant startedAt, JsonNode serviceInput,
        JsonNode serviceOutput, Map<String, JsonNode> variables,
        Map<String, JsonNode> context, Map<String, NodeExecutionResult> nodeResults,
        ExecutionConfig config, Map<String, String> metadata
    ) {
        public static ExecutionContextBuilder builder() {
            return new ExecutionContextBuilder();
        }
    }

    // === 上下文操作 ===
    public void set(String key, JsonNode value);
    public JsonNode get(String key);
    public JsonNode getVariable(String name);
    public ExecutionContext createChild();  // 创建子上下文（用于并行）
    public JsonNode toSnapshot();  // 生成快照（用于审计）
    public void initializeWorkflowVariables();
}
```

---

### 6.2 WorkflowNode 节点类型（多态设计）

**参考 Flowable/Camunda 的节点继承架构**

```java
// 基础抽象类
public abstract class WorkflowNode {
    protected final String id;
    protected final String name;
    protected final NodeType type;
    protected final String description;
    protected final ObjectNode configuration;  // JSON配置

    protected WorkflowNode(String id, String name, NodeType type,
                          String description, ObjectNode configuration);

    // === 核心方法 ===
    public abstract NodeExecutor createExecutor(ExecutionContext context);
    public abstract void validate() throws NodeValidationException;

    // === 公共方法 ===
    public String id();
    public String name();
    public NodeType type();
}
```

**节点类型枚举**:

```java
public enum NodeType {
    START,           // 起始节点
    END,             // 结束节点
    DATASOURCE,      // 数据源节点
    CONDITIONAL,     // 条件分支（If/Else）
    LOOP,            // 循环节点
    TRANSFORM,       // 数据转换
}
```

---

### 6.2.1 DatasourceNode - 数据源节点

```java
public final class DatasourceNode extends WorkflowNode {

    private final DatasourceId datasourceId;
    private final Integer datasourceVersion;
    private final NodeMapping inputMapping;   // 从上下文获取输入
    private final NodeMapping outputMapping;  // 将结果写入上下文

    public DatasourceNode(String id, String name,
                          DatasourceId datasourceId, Integer datasourceVersion,
                          NodeMapping inputMapping, NodeMapping outputMapping);

    @Override
    public NodeExecutor createExecutor(ExecutionContext context) {
        return new DatasourceNodeExecutor(
            datasourceId, datasourceVersion, inputMapping, outputMapping);
    }

    @Override
    public void validate() {
        if (datasourceId == null) {
            throw new NodeValidationException(id, "datasourceId is required");
        }
        // ... 其他验证
    }
}
```

---

### 6.2.2 ConditionalNode - 条件分支节点

```java
public final class ConditionalNode extends WorkflowNode {

    private final String condition;        // SpEL表达式
    private final String trueBranchId;     // true路径
    private final String falseBranchId;    // false路径

    public ConditionalNode(String id, String name,
                          String condition,
                          String trueBranchId,
                          String falseBranchId);

    @Override
    public NodeExecutor createExecutor(ExecutionContext context) {
        return new ConditionalNodeExecutor(id, condition, trueBranchId, falseBranchId);
    }

    @Override
    public void validate() {
        if (condition == null || condition.isBlank()) {
            throw new NodeValidationException(id, "condition is required");
        }
    }
}
```

---

### 6.2.3 LoopNode - 循环节点

```java
public final class LoopNode extends WorkflowNode {

    private final String iterableExpression;  // SpEL表达式
    private final String itemVariable;        // 循环变量
    private final String itemIndexVariable;   // 索引变量
    private final String maxIterations;       // 最大迭代次数

    public LoopNode(String id, String name,
                   String iterableExpression,
                   String itemVariable,
                   String itemIndexVariable,
                   String maxIterations);

    @Override
    public NodeExecutor createExecutor(ExecutionContext context) {
        return new LoopNodeExecutor(id, iterableExpression, itemVariable,
                                   itemIndexVariable, maxIterations);
    }

    @Override
    public void validate() {
        // ... 验证逻辑
    }
}
```

---

### 6.2.4 TransformNode - 转换节点

```java
public final class TransformNode extends WorkflowNode {

    private final TransformType transformType;  // JSON_PATH, EXPRESSION, SCRIPT
    private final String expression;
    private final String outputVariable;

    public TransformNode(String id, String name,
                        TransformType transformType,
                        String expression,
                        String outputVariable);

    @Override
    public NodeExecutor createExecutor(ExecutionContext context) {
        return new TransformNodeExecutor(id, transformType, expression, outputVariable);
    }

    @Override
    public void validate() {
        // ... 验证逻辑
    }
}

public enum TransformType {
    JSON_PATH,    // JsonPath表达式
    EXPRESSION,   // SpEL表达式
    SCRIPT        // 脚本（Groovy/JavaScript）
}
```

---

### 6.2.5 StartNode / EndNode

```java
public final class StartNode extends WorkflowNode {
    public StartNode(String id, String name);

    @Override
    public NodeExecutor createExecutor(ExecutionContext context) {
        return new StartNodeExecutor(id);
    }

    @Override
    public void validate() {
        // Start节点无需验证
    }
}

public final class EndNode extends WorkflowNode {
    public EndNode(String id, String name);

    @Override
    public NodeExecutor createExecutor(ExecutionContext context) {
        return new EndNodeExecutor(id);
    }

    @Override
    public void validate() {
        // End节点无需验证
    }
}
```

---

### 6.3 WorkflowDAGExecutor 执行器

**职责**: DAG执行引擎，基于连接的隐式并行模型

```java
@Service
public class WorkflowDAGExecutor {

    /**
     * 执行DAG工作流
     */
    public Mono<WorkflowExecution> execute(WorkflowGraph graph, WorkflowExecution execution) {
        ExecutionContext context = buildExecutionContext(execution);

        return executeDAG(graph, context)
            .map(result -> {
                execution.markSuccess(result);
                return execution;
            })
            .onErrorResume(error -> {
                execution.markFailure(error.getMessage());
                return Mono.just(execution);
            });
    }

    private Mono<JsonNode> executeDAG(WorkflowGraph graph, ExecutionContext context) {
        // 1. 找到起始节点
        List<String> startNodes = graph.findStartNodes();

        // 2. 创建待执行队列
        Queue<NodeExecutionTask> pendingTasks = new LinkedList<>();

        // 3. 将起始节点加入队列
        for (String nodeId : startNodes) {
            pendingTasks.add(new NodeExecutionTask(nodeId, context));
        }

        // 4. 记录已完成节点和结果
        Set<String> completedNodes = new HashSet<>();
        Map<String, List<JsonNode>> nodeResults = new ConcurrentHashMap<>();
        Map<String, CountDownLatch> nodeLatches = new ConcurrentHashMap<>();

        return executeTasks(graph, pendingTasks, completedNodes,
                          nodeResults, nodeLatches, context);
    }

    private Mono<JsonNode> executeTasks(
            WorkflowGraph graph,
            Queue<NodeExecutionTask> pendingTasks,
            Set<String> completedNodes,
            Map<String, List<JsonNode>> nodeResults,
            Map<String, CountDownLatch> nodeLatches,
            ExecutionContext context) {

        if (pendingTasks.isEmpty()) {
            // 找到结束节点，返回最终输出
            String endNodeId = findEndNode(graph);
            return Mono.just(context.get("#finalOutput"));
        }

        // 提取可以执行的批次（无依赖或依赖已满足）
        List<NodeExecutionTask> currentBatch = extractReadyTasks(
            pendingTasks, graph, completedNodes);

        // 并行执行批次中的节点
        return Flux.fromIterable(currentBatch)
            .flatMap(task -> executeNode(graph, task, nodeResults,
                                       nodeLatches, context))
            .collectList()
            .flatMap(results -> {
                // 查找下一批可执行的节点
                List<String> nextNodeIds = findNextExecutableNodes(
                    graph, completedNodes, nodeResults);

                for (String nodeId : nextNodeIds) {
                    pendingTasks.add(new NodeExecutionTask(nodeId, context));
                }

                return executeTasks(graph, pendingTasks, completedNodes,
                                  nodeResults, nodeLatches, context);
            });
    }

    private Mono<JsonNode> executeNode(
            WorkflowGraph graph,
            NodeExecutionTask task,
            Map<String, List<JsonNode>> nodeResults,
            Map<String, CountDownLatch> nodeLatches,
            ExecutionContext context) {

        WorkflowNode node = graph.nodes().get(task.nodeId);

        // 扇入：等待所有上游节点完成
        List<WorkflowConnection> inbound = graph.getInboundConnections(task.nodeId);
        if (inbound.size() > 1) {
            waitForAllUpstream(nodeLatches, inbound, task.nodeId);
            JsonNode mergedInput = mergeUpstreamResults(inbound, nodeResults, context);
            context.set("#nodeInput", mergedInput);
        }

        // 执行节点
        NodeExecutor executor = node.createExecutor(context);
        return executor.execute(context)
            .map(result -> {
                nodeResults.computeIfAbsent(task.nodeId, k -> new ArrayList<>())
                    .add(result.output());
                return result.output();
            })
            .doOnSuccess(output -> {
                // 通知下游节点
                List<WorkflowConnection> outbound = graph.getOutboundConnections(task.nodeId);
                for (WorkflowConnection conn : outbound) {
                    CountDownLatch latch = nodeLatches.computeIfAbsent(
                        conn.targetNodeId(),
                        k -> new CountDownLatch(inbound.size())
                    );
                    latch.countDown();
                }
            });
    }

    /**
     * 扇入合并逻辑
     */
    private JsonNode mergeUpstreamResults(
            List<WorkflowConnection> inbound,
            Map<String, List<JsonNode>> nodeResults,
            ExecutionContext context) {

        List<JsonNode> upstreamResults = inbound.stream()
            .map(conn -> {
                List<JsonNode> results = nodeResults.get(conn.sourceNodeId());
                return results.get(results.size() - 1);
            })
            .toList();

        MergeStrategy strategy = inbound.get(0).mergeStrategy();

        return switch (strategy) {
            case ARRAY -> {
                ArrayNode array = objectMapper.createArrayNode();
                upstreamResults.forEach(array::add);
                yield array;
            }
            case FIRST_SUCCESS -> {
                yield upstreamResults.get(0);
            }
            case ALL_SUCCESS -> {
                ObjectNode merged = objectMapper.createObjectNode();
                for (int i = 0; i < inbound.size(); i++) {
                    WorkflowConnection conn = inbound.get(i);
                    merged.put(conn.outputPort(), upstreamResults.get(i));
                }
                yield merged;
            }
        };
    }
}
```

---

### 6.4 并行执行示例

```
场景：同时调用2个数据源，然后合并结果继续处理

    [Start Node]
         |
         v
    [并行扇出 - 隐式]
         |
    +----+----+
    |         |
    v         v
[DS Node1] [DS Node2]   <- 并行执行
    |         |
    +----+----+
         |
         v
    [扇入合并 - ARRAY策略]
         |
         v
  [Transform Node]  <- 接收 [[result1], [result2]]
         |
         v
    [DS Node3]       <- 使用转换后的结果
         |
         v
    [End Node]

连接定义：
{
  "connections": [
    {
      "source": "start",
      "target": "ds-user",
      "mergeStrategy": "ARRAY"
    },
    {
      "source": "start",
      "target": "ds-order",
      "mergeStrategy": "ARRAY"
    },
    {
      "source": "ds-user",
      "target": "transform"
    },
    {
      "source": "ds-order",
      "target": "transform"
    },
    {
      "source": "transform",
      "target": "ds-save"
    }
  ]
}
```

---

## 7. 现有模型优化建议

### 7.1 ApiService 需要重构

**问题**:
- 早期设计只支持单Datasource绑定
- 缺少Workflow运行模式

**优化方案**: 引入`ServiceMode`和`ServiceBinding`

```java
// 新增枚举
public enum ServiceMode {
    DATASOURCE,  // 直接调用Datasource
    WORKFLOW     // 执行Workflow
}

// 新增通用绑定对象
public final class ServiceBinding implements ValueObject<ServiceBinding> {
    private final ServiceMode mode;
    private final DatasourceId datasourceId;  // DATASOURCE模式
    private final WorkflowId workflowId;       // WORKFLOW模式
    private final Map<String, FieldBinding> inputMapping;
    private final Map<String, FieldBinding> outputMapping;
}

// ApiService简化
public final class ApiService {
    // 移除：
    // - private DatasourceId datasourceId;
    // - private ServiceMapping mapping;

    // 新增：
    private ServiceBinding binding;
}
```

---

### 7.2 ServiceMapping 可作为通用组件

**问题**: 早期设计中ServiceMapping与ApiDatasource耦合

**优化方案**: ServiceMapping作为独立值对象，支持映射到不同目标

```java
public final class ServiceMapping implements ValueObject<ServiceMapping> {
    private final ServiceMode mode;
    private final DatasourceId datasourceId;  // DATASOURCE模式
    private final WorkflowId workflowId;       // WORKFLOW模式
    private final Map<String, FieldBinding> inputMapping;
    private final Map<String, FieldBinding> outputMapping;
}
```

---

### 7.3 ServiceContract 不要引入Jackson

**问题**: 如果在ServiceContract中引入JsonNode，会让领域模型依赖基础设施

**正确做法**:

```java
// ✅ 正确：使用字符串
public final class ServiceContract implements ValueObject<ServiceContract> {
    private final String inputSchema;   // JSON Schema字符串
    private final String outputSchema;  // JSON Schema字符串
}

// ❌ 错误：引入Jackson
public final class ServiceContract implements ValueObject<ServiceContract> {
    private final JsonNode inputSchema;  // 不要这样做！
    private final JsonNode outputSchema;
}

// 验证逻辑放在领域服务
@Service
public class SchemaValidationServiceImpl implements SchemaValidationService {
    private final ObjectMapper objectMapper;  // Jackson在这里
}
```

---

### 7.4 DatasourceContract 不应独立版本

**问题**: 如果Contract有独立版本，可能导致Contract和Datasource版本不一致

**正确做法**:

```java
// ✅ 正确：Contract随Datasource版本化
public final class ApiDatasource implements DomainEntity<ApiDatasource> {
    private final DatasourceId id;  // (key, version)
    private DatasourceContract contract;  // 没有单独版本

    // 更新Contract需要创建新的Datasource版本
    public void updateContract(DatasourceContract newContract) {
        if (isReferenced) {
            throw new DatasourceReferencedException(id);
        }
        this.contract = newContract;
    }
}
```

---

### 7.5 DatasourceOperation 简化

**问题**:

1. `sameValueAs()` 总是返回 `false`（见 `HttpDatasourceOperation:63`）
2. 参数使用 `Map<String, Object>`，类型不安全
3. 不需要 `successCriteria`（这是执行引擎的职责）

**优化方案**:

```java
// ❌ 移除：作为ValueObject不合适
public interface DatasourceOperation extends ValueObject<DatasourceOperation> {
    boolean sameValueAs(DatasourceOperation other);  // 不需要
    Specification<OperationResult> successCriteria();  // 不需要
}

// ✅ 正确：作为规格或配置对象
public interface DatasourceOperation {
    OperationType type();
    boolean isCompatibleWith(DatasourceConnection connection);
}

// 具体实现使用明确的类型
public final class HttpDatasourceOperation implements DatasourceOperation {
    private final HttpMethod method;
    private final String path;
    private final HttpRequestHeaders headers;
    private final Duration timeout;

    // 不再需要 sameValueAs
    // 不再需要 successCriteria

    @Override
    public boolean isCompatibleWith(HttpDatasourceConnection connection) {
        return true;
    }
}
```

---

### 7.6 扩展点 Extension 增强

**问题**: 当前仅存储名称

**优化方案**:

```java
// ❌ 当前：过于简单
public record Extension(String name) { }

// ✅ 优化：增加类型和配置
public final class Extension implements ValueObject<Extension> {
    private final ExtensionType type;    // FILTER, TRANSFORMER, VALIDATOR
    private final String name;
    private final Configuration config;  // 扩展配置

    public enum ExtensionType {
        FILTER,       // 过滤器（日志、指标、缓存等）
        TRANSFORMER,  // 转换器（数据转换）
        VALIDATOR     // 验证器（业务规则）
    }
}
```

---

### 7.7 领域事件需要发布

**问题**: 领域事件被收集但从未发布

**优化方案**: 在Repository中发布事件

```java
// 领域事件发布器接口
public interface DomainEventPublisher {
    void publish(Object domainEvent);
}

// 在Repository中集成
public class R2dbcApiDatasourceRepository implements ApiDatasourceRepository {
    private final DomainEventPublisher eventPublisher;

    @Override
    public Mono<ApiDatasource> save(ApiDatasource ds) {
        return apiDatasourceEntityRepository.save(...)
            .map(ApiDatasourceEntity::toApiDatasource)
            .doOnNext(saved -> {
                // 发布领域事件
                ds.pullDomainEvents().forEach(eventPublisher::publish);
            });
    }
}
```

---

### 7.8 工作流节点不需要额外的 inputMapping/outputMapping

**问题**: 考虑是否需要在WorkflowNode上增加mapping

**结论**: 不需要

**原因**:

1. **Datasource已有Contract**: 每个Datasource的`contract.inputSchema`和`contract.outputSchema`已经明确定义
2. **使用全局上下文**: 所有节点共享`ExecutionContext`，通过SpEL表达式读取/写入
3. **必要时使用独立转换节点**: 复杂转换通过`TransformNode`处理

**正确做法**:

```java
// ✅ 正确：使用表达式从上下文读取
public final class DatasourceNode extends WorkflowNode {
    private final String inputExpression;   // "{{ #context.userData }}"
    private final String outputVariable;    // "#context.processedData"
}

// 数据转换通过独立的TransformNode处理
public final class TransformNode extends WorkflowNode {
    private final TransformType transformType;
    private final String expression;  // 转换逻辑
}
```

---

## 8. 业务规则与不变量

### 8.1 Workflow 业务规则

| 规则编号 | 规则描述 | 违规后果 |
|---------|---------|---------|
| WF-1 | 只有DRAFT状态的工作流可以修改结构 | 抛出 `WorkflowNotDraftException` |
| WF-2 | 激活前必须验证图的有效性（无环） | 抛出 `WorkflowInvalidException` |
| WF-3 | 被Service引用的Workflow不能删除、修改、归档 | 抛出 `WorkflowReferencedException` |
| WF-4 | 激活的工作流不能直接修改，需创建新版本 | 通过版本机制强制 |
| WF-5 | 工作流必须有起始节点和结束节点 | 激活时验证 |
| WF-6 | 节点配置完整才能激活 | 抛出 `NodeValidationException` |

---

### 8.2 ApiService 业务规则

| 规则编号 | 规则描述 | 违规后果 |
|---------|---------|---------|
| AS-1 | 只有DISABLED状态的Service可以修改绑定 | 抛出 `ServiceNotDisabledException` |
| AS-2 | 引用的Datasource必须存在且为ENABLED状态 | 抛出 `DatasourceNotFoundException` 或 `DatasourceNotEnabledException` |
| AS-3 | 引用的Workflow必须存在且为ACTIVE状态 | 抛出 `WorkflowNotFoundException` 或 `WorkflowNotActiveException` |
| AS-4 | 只有ENABLED的Service才能被调用 | 执行前检查 |

---

### 8.3 ApiDatasource 业务规则（保持不变）

| 规则编号 | 规则描述 | 违规后果 |
|---------|---------|---------|
| DS-1 | 被引用的Datasource不可修改核心字段 | 抛出 `DatasourceReferencedException` |
| DS-2 | 只有ENABLED状态的Datasource才能被调用 | 执行前检查 |
| DS-3 | 不允许删除Datasource | Repository不提供`delete()`方法 |
| DS-4 | 必须支持版本 | `DatasourceId`包含`(key, version)` |

---

### 8.4 WorkflowExecution 业务规则

| 规则编号 | 规则描述 | 违规后果 |
|---------|---------|---------|
| WE-1 | 只有RUNNING状态的执行可以取消 | 抛出 `ExecutionNotRunningException` |
| WE-2 | 执行超时自动标记为TIMEOUT | 执行引擎定时检查 |
| WE-3 | 节点执行失败根据FailureStrategy决定是否继续 | 继续/停止/暂停 |

---

## 9. 存储策略

### 9.1 数据库表设计

#### FLW_WORKFLOW 表

```sql
CREATE TABLE FLW_WORKFLOW (
    ID_            BIGSERIAL PRIMARY KEY,
    KEY_           VARCHAR(64)              NOT NULL,
    VERSION_       INT                      NOT NULL,
    STATUS_        VARCHAR(16)              NOT NULL,  -- DRAFT, ACTIVE, ARCHIVED
    NAME_          VARCHAR(128)             NOT NULL,
    DESCRIPTION_   VARCHAR(512),
    GRAPH_         JSONB                    NOT NULL,  -- WorkflowGraph
    CONFIG_        JSONB                    NOT NULL,  -- WorkflowConfiguration
    VARIABLES_     JSONB                    NOT NULL,  -- List<WorkflowVariable>
    CREATED_BY_    VARCHAR(64),
    CREATED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    UPDATED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (KEY_, VERSION_)
);

CREATE INDEX IDX_WORKFLOW_STATUS ON FLW_WORKFLOW(STATUS_);
CREATE INDEX IDX_WORKFLOW_KEY ON FLW_WORKFLOW(KEY_);
```

#### FLW_WORKFLOW_EXECUTION 表

```sql
CREATE TABLE FLW_WORKFLOW_EXECUTION (
    ID_            VARCHAR(36) PRIMARY KEY,  -- executionId (UUID)
    WORKFLOW_KEY_  VARCHAR(64)              NOT NULL,
    WORKFLOW_VERSION_ INT                   NOT NULL,
    STATUS_        VARCHAR(16)              NOT NULL,
    INPUT_         JSONB,
    OUTPUT_        JSONB,
    STARTED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    FINISHED_AT_   TIMESTAMP WITH TIME ZONE,
    DURATION_MS_   BIGINT,
    ERROR_         VARCHAR(4096),
    CONTEXT_SNAPSHOT_ JSONB,
    METADATA_      JSONB,
    CREATED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (WORKFLOW_KEY_, WORKFLOW_VERSION_)
        REFERENCES FLW_WORKFLOW(KEY_, VERSION_)
);

CREATE INDEX IDX_EXECUTION_STATUS ON FLW_WORKFLOW_EXECUTION(STATUS_);
CREATE INDEX IDX_EXECUTION_WORKFLOW ON FLW_WORKFLOW_EXECUTION(WORKFLOW_KEY_);
CREATE INDEX IDX_EXECUTION_TIME ON FLW_WORKFLOW_EXECUTION(STARTED_AT_);
```

#### FLW_NODE_EXECUTION 表

```sql
CREATE TABLE FLW_NODE_EXECUTION (
    ID_            BIGSERIAL PRIMARY KEY,
    EXECUTION_ID_  VARCHAR(36)              NOT NULL,
    NODE_ID_       VARCHAR(64)              NOT NULL,
    STATUS_        VARCHAR(16)              NOT NULL,
    STARTED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    FINISHED_AT_   TIMESTAMP WITH TIME ZONE,
    DURATION_MS_   BIGINT,
    INPUT_         JSONB,
    OUTPUT_        JSONB,
    ERROR_         VARCHAR(4096),
    ERROR_STACK_   TEXT,
    ATTEMPT_       INT                      NOT NULL DEFAULT 1,
    MAX_ATTEMPTS_  INT,
    CREATED_AT_    TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (EXECUTION_ID_)
        REFERENCES FLW_WORKFLOW_EXECUTION(ID_),
    INDEX IDX_NODE_EXECUTION_EXECUTION (EXECUTION_ID_),
    INDEX IDX_NODE_EXECUTION_NODE (EXECUTION_ID_, NODE_ID_)
);
```

#### FLW_API_SERVICE 表（重构）

```sql
CREATE TABLE FLW_API_SERVICE (
    ID_                BIGSERIAL PRIMARY KEY,
    SERVICE_ID_        VARCHAR(64)              NOT NULL UNIQUE,
    NAME_              VARCHAR(64)              NOT NULL,
    DESCRIPTION_       VARCHAR(256)             NOT NULL,
    STATUS_            VARCHAR(16)              NOT NULL,
    INPUT_SCHEMA_      JSONB                    NOT NULL,
    OUTPUT_SCHEMA_     JSONB                    NOT NULL,
    BINDING_MODE_      VARCHAR(16)              NOT NULL,  -- DATASOURCE, WORKFLOW
    BINDING_CONFIG_    JSONB                    NOT NULL,  -- ServiceBinding (序列化)
    CREATED_AT_        TIMESTAMP WITH TIME ZONE NOT NULL,
    UPDATED_AT_        TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IDX_SERVICE_STATUS ON FLW_API_SERVICE(STATUS_);
```

---

### 9.2 JSONB 存储格式

#### WorkflowGraph 格式

```json
{
  "nodes": {
    "start": {
      "type": "START",
      "name": "Start",
      "description": "Workflow start"
    },
    "ds-user": {
      "type": "DATASOURCE",
      "name": "Get User",
      "datasourceId": {
        "key": "user-api",
        "version": 1
      },
      "inputMapping": {
        "userId": "{{ #context.request.userId }}"
      },
      "outputMapping": {
        "outputVariable": "#context.userData"
      }
    },
    "ds-order": {
      "type": "DATASOURCE",
      "name": "Get Order",
      "datasourceId": {
        "key": "order-api",
        "version": 2
      },
      "inputMapping": {
        "orderId": "{{ #context.request.orderId }}"
      },
      "outputMapping": {
        "outputVariable": "#context.orderData"
      }
    },
    "end": {
      "type": "END",
      "name": "End",
      "description": "Workflow end"
    }
  },
  "connections": [
    {
      "sourceNodeId": "start",
      "targetNodeId": "ds-user",
      "outputPort": "output",
      "inputPort": "input",
      "mergeStrategy": "ARRAY"
    },
    {
      "sourceNodeId": "start",
      "targetNodeId": "ds-order",
      "outputPort": "output",
      "inputPort": "input",
      "mergeStrategy": "ARRAY"
    },
    {
      "sourceNodeId": "ds-user",
      "targetNodeId": "end"
    },
    {
      "sourceNodeId": "ds-order",
      "targetNodeId": "end"
    }
  ]
}
```

#### ServiceBinding 格式

```json
{
  "mode": "WORKFLOW",
  "workflowId": {
    "key": "process-order",
    "version": 2
  },
  "inputMapping": {
    "orderContext": "{{ #serviceInput }}"
  },
  "outputMapping": {
    "result": "{{ #targetOutput.finalResult }}"
  }
}
```

---

## 10. 总结

### 10.1 核心设计决策

1. **三个独立聚合根**: ApiDatasource、ApiService、Workflow
2. **Workflow独立于Datasource**: ApiService通过mode选择执行路径
3. **隐式并行模型**: 通过连接关系表达并行，无需Fork/Join节点
4. **统一ExecutionContext**: 支持单节点和Workflow执行
5. **节点多态设计**: 不同节点类型有独立的实现类（参考Flowable/Camunda）
6. **完整执行历史**: WorkflowExecution + NodeExecution
7. **值对象不引入第三方库**: Contract使用字符串，验证在领域服务

### 10.2 主要优化点

| 领域对象 | 优化点 |
|---------|--------|
| ApiService | 引入ServiceMode和ServiceBinding支持Workflow模式 |
| ServiceMapping | 作为通用组件，支持绑定到不同目标 |
| ServiceContract | 使用字符串而非JsonNode，验证逻辑在领域服务 |
| DatasourceContract | 随ApiDatasource版本化，无独立版本 |
| DatasourceOperation | 移除successCriteria，简化为规格对象 |
| Extension | 增加ExtensionType和Configuration |
| 领域事件 | 在Repository中集成发布 |

### 10.3 MVP 范围

**包含**:
- ✅ 基本串行执行
- ✅ 并行执行（基于连接的隐式并行）
- ✅ 条件分支（If/Else）
- ✅ 数据转换节点
- ✅ 完整执行历史
- ✅ ExecutionContext统一上下文

**排除**:
- ❌ 循环迭代（后续版本）
- ❌ 子工作流调用（后续版本）
- ❌ 暂停和恢复执行（后续版本）

### 10.4 待完善项

1. **节点配置验证**: 各节点类型的validate()方法具体实现
2. **错误处理策略**: FailureStrategy的具体处理逻辑
3. **性能优化**: 大规模节点的执行效率优化
4. **监控和观测**: 指标收集、分布式追踪
5. **安全控制**: API Key、JWT认证等（暂不考虑）

---

**文档版本**: 1.0
**最后更新**: 2025-01-17
**作者**: Claude + 人工设计
