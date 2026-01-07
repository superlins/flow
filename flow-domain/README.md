# ApiDatasource 领域模型设计意图（Design Intent）

本文档系统性地阐述 ApiDatasource 领域模型的全部设计意图，可作为架构设计文档、ADR（Architecture Decision Record）或模块 README 使用。

---

## 1. 领域定位（Why ApiDatasource Exists）

### 1.1 核心职责

**ApiDatasource 表达的是：**

👉 **"一个可被 ApiService 引用、可执行、具备明确契约承诺的底层数据源能力"**

**它不是：**
- ❌ 数据库连接池
- ❌ HTTP 客户端
- ❌ SQL 或 URL 的简单配置集合
- ❌ 执行结果

**它是一个对上承诺、对下封装的稳定能力单元。**

---

### 1.2 与 ApiService 的边界

| 维度       | ApiDatasource | ApiService       |
|----------|---------------|------------------|
| 面向对象     | 平台 / 技术侧      | 业务 / 产品侧         |
| 是否可直接调用  | 是             | 否（通过 Datasource） |
| 是否有版本    | 有             | 无                |
| 是否定义底层协议 | 是             | 否                |
| 是否暴露给用户  | 否             | 是                |

**ApiDatasource 的 Contract 是对 ApiService 的承诺，而不是实现细节**

---

## 2. 聚合设计（Aggregate Design）

### 2.1 聚合根选择

**ApiDatasource 是一个聚合根**

**理由：**
- 拥有完整生命周期（创建 → 启用 → 停用）
- 有不可破坏的业务不变量（被引用不可修改）
- 是 ApiService 的稳定依赖点

---

### 2.2 聚合一致性边界

**一个 ApiDatasource 聚合内，必须始终一致：**
- `ContractSpec`
- `OperationSpec`
- `ConnectionSpec`
- `Status`

**任何一个变化都可能影响调用正确性，因此：**

**它们不能拆成独立聚合**

---

## 3. 标识设计（Identity）

### 3.1 ApiDatasourceId

**(key, version)**

**设计意图：**
- 业务可读
- 可稳定引用
- 显式支持版本演进
- 不暴露数据库自增 ID

---

### 3.2 为什么要有 Version

- 支持 **无破坏升级**
- 支持 **并行版本**
- 支持 **逐步迁移 ApiService 引用**
- 完全符合你定义的 **DS-1 规则**

---

## 4. Contract 设计（核心设计点）

### 4.1 Contract 是"法律"，不是"实现"

**ContractSpec：**
- 定义 ApiService 必须满足的输入
- 定义 Datasource 承诺返回的输出

**它的作用对象是：**
- ApiService（绑定阶段）
- 执行引擎（执行前 / 执行后）

---

### 4.2 为什么 Contract 在 Datasource 中

**因为：**

**ApiService 的契约来源于 ApiDatasource，但可以被重塑**

Datasource 负责给出"最低要求"，ApiService 可以：
- 重命名字段
- 合并 / 拆分字段
- 重新计算输出

**但不能违反 Datasource 的 Contract**

---

### 4.3 JSON Schema 属于领域规则吗？

**是的。**

**原因：**
- 是否满足 Schema = 是否允许执行
- 不满足 = 业务失败，不是技术异常
- `strict` / `default` / `const` 都是业务约束

**因此：**

**Schema 的解析与校验逻辑属于领域模型的一部分**

---

## 5. OperationSpec

### 5.1 设计意图

**OperationSpec 表达的是：**

👉 **"这个 Datasource 要做什么操作"**

**而不是：**
- ❌ 如何连接
- ❌ 如何重试
- ❌ 如何鉴权

---

### 5.2 为什么 sql / method / path 在 Operation

| 内容           | 属于        |
|--------------|-----------|
| SQL          | Operation |
| HTTP method  | Operation |
| HTTP URL     | Operation |
| HTTP PARAMS  | Operation |
| HTTP BODY    | Operation |
| HTTP HEADERS | Operation |

**因为这些决定的是"行为"，不是"通道"**

---

## 6. ConnectionSpec

### 6.1 职责

**ConnectionSpec 只描述：**

👉 **"如何连到目标系统"**

**它不关心：**
- 具体操作内容
- 输入输出结构
- 字段映射

---

### 6.2 为什么不把 sql / path 放进 connection

**因为那会导致：**
- 不可复用
- 职责混乱
- 后续无法支持多个 Operation 共用一个连接

---

## 7. Extension（插件体系设计意图）

### 7.1 Extension 是"声明"，不是"行为"

```json
{
  "extension": [
    { "id": "oauth2-enricher@1.0.0" }
  ]
}
```

**设计意图：**
- Datasource 只声明依赖
- 插件配置在插件自己的存储中
- 执行期由引擎统一装配

---

### 7.2 为什么 Extension 不进入 Contract

**因为：**
- 插件不应影响 ApiService 的绑定合法性
- 插件是执行期 concern，不是契约 concern

---

## 8. Options（受控扩展点）

### 8.1 为什么需要 Options

- 支持灰度能力
- 支持实验性功能
- 避免频繁表结构变更

---

### 8.2 Options 的铁律

**❌ Options 不允许承载：**
- Contract
- Operation
- Connection

**否则一定会演化成"垃圾桶字段"。**

---

## 9. 业务规则设计意图（DS-1 ～ DS-5）

### DS-1 不可修改被引用的 Datasource

**设计目标：**
- 保证 ApiService 稳定性
- 防止隐式破坏
- 强制版本演进

**实现方式：**
- Repository 提供 `isReferenced()` 方法
- 修改前必须检查引用状态
- 被引用时强制创建新版本

---

### DS-2 只有 Enabled 才允许调用

**设计目标：**
- 运维可控
- 即时止损
- 无后门

**实现方式：**
- `ApiDatasource.isEnabled()` 方法
- 执行引擎在执行前必须检查状态
- 状态变更通过领域事件通知

---

### DS-3 无并发 / 独占假设

**设计目标：**
- 最大通用性
- 不绑定执行模型

**实现方式：**
- 领域模型不包含并发控制逻辑
- 由执行引擎或基础设施层处理并发

---

### DS-4 不允许删除

**设计目标：**
- 审计
- 可追溯
- 历史可复现

**实现方式：**
- Repository 不提供 `delete()` 方法
- 只能通过 `disable()` 停用
- 所有历史版本保留

---

### DS-5 必须支持版本

**设计目标：**
- 长期演进
- 多服务共存
- 平滑迁移

**实现方式：**
- `DatasourceId` 包含 `(key, version)`
- `findByKey()` 返回所有版本
- 版本号必须为正整数

---

## 10. Repository 设计意图

### 10.1 Repository 的唯一职责

**在领域模型与数据库模型之间翻译语义**

**它：**
- 知道 JSONB
- 知道表结构
- 知道多态反序列化

**领域模型完全不关心这些细节**

---

### 10.2 Repository 接口设计

```java
public interface ApiDatasourceRepository {
    Mono<ApiDatasource> findById(DatasourceId id);
    Flux<ApiDatasource> findByKey(String key);
    Mono<ApiDatasource> save(ApiDatasource datasource);
    Mono<Boolean> isReferenced(DatasourceId id);
}
```

**设计原则：**
- 使用领域模型类型（`DatasourceId`，不是 `Long`）
- 返回领域模型（`ApiDatasource`，不是 `Entity`）
- 隐藏持久化细节（JSONB、表结构等）

---

## 11. 领域事件设计意图

### 11.1 事件类型

- `ApiDatasourceCreatedEvent` - 创建时发布
- `ApiDatasourceEnabledEvent` - 启用时发布
- `ApiDatasourceDisabledEvent` - 停用时发布

### 11.2 事件作用

- 通知 ApiService 状态变更
- 触发执行引擎缓存刷新
- 支持审计日志记录

---

## 12. 设计原则总结

### 12.1 核心原则

1. **契约优先**：Contract 是 ApiDatasource 的核心，定义了与 ApiService 的边界
2. **职责分离**：Operation 定义行为，Connection 定义通道，Contract 定义契约
3. **版本演进**：通过版本机制支持无破坏升级和平滑迁移
4. **稳定性保证**：被引用的 Datasource 不可修改，保证 ApiService 稳定性

### 12.2 设计约束

- ❌ 不允许删除 Datasource
- ❌ 不允许修改被引用的 Datasource
- ❌ Options 不允许承载核心领域概念
- ❌ Extension 不影响 Contract 合法性

---

## 13. 后续演进方向

### 13.1 待完善点

1. **多 Operation 支持**：一个 Datasource 支持多个 Operation（当前为单一 Operation）
2. **Connection 复用**：多个 Operation 共享同一个 Connection
3. **Schema 版本管理**：Contract 的 Schema 也需要版本化
4. **插件配置管理**：Extension 的配置存储和加载机制

### 13.2 扩展点

- **执行引擎集成**：如何将 ApiDatasource 转换为可执行单元
- **监控与观测**：如何追踪 Datasource 的执行状态
- **性能优化**：Connection 池化、缓存策略等

---

## 附录：关键代码位置

- **聚合根**：`com.zwtech.flow.domain.model.apidatasource.ApiDatasource`
- **标识**：`com.zwtech.flow.domain.model.apidatasource.DatasourceId`
- **契约**：`com.zwtech.flow.domain.model.apidatasource.DatasourceContract`
- **操作**：`com.zwtech.flow.domain.model.apidatasource.operation.OperationSpec`
- **连接**：`com.zwtech.flow.domain.model.apidatasource.connection.ConnectionSpec`
- **仓储**：`com.zwtech.flow.domain.model.apidatasource.ApiDatasourceRepository`
- **实现**：`com.zwtech.flow.domain.model.apidatasource.r2dbc.R2dbcApiDatasourceRepository`

---

---

## 14. 实现状态与待完善项

### 14.1 已实现

✅ **核心领域模型**
- ApiDatasource 聚合根完整实现
- DatasourceId (key, version) 标识设计
- DatasourceContract 契约设计
- OperationSpec 和 ConnectionSpec 职责分离
- Extension 扩展声明（无 version，版本通过 DatasourceId 管理）

✅ **领域行为**
- `create()` - 创建新的 Datasource
- `configure()` - 配置核心字段（一次性配置）
- `updateMetadata()` - 更新可变字段（name、description）
- `updateCoreFields()` - 更新核心字段（实现 DS-1 规则检查）
- `updateExtensions()` - 更新扩展列表
- `enable()` / `disable()` - 状态管理（实现 DS-2 规则）

✅ **Repository 设计**
- 领域模型与持久化模型分离
- 使用 `restore()` 静态工厂方法恢复领域对象
- 语义翻译职责明确

### 14.2 待完善

⚠️ **JSON 序列化/反序列化**
- OperationSpec 和 ConnectionSpec 的多态序列化
- Extension 列表的 JSON 数组序列化
- 需要集成 Jackson 或类似 JSON 库
- 使用 `@JsonTypeInfo` 和 `@JsonSubTypes` 实现多态

⚠️ **isReferenced() 实现**
- 需要查询 FLW_API_SERVICE 表
- 检查 datasourceId 引用关系
- 当前返回 false（占位实现）

⚠️ **数据库字段**
- 需要添加 `STRICT_` 字段到 FLW_API_DATASOURCE 表
- 用于存储 DatasourceContract 的 strict 标志

### 14.3 设计决策记录

**决策 1：ApiDatasource 的修改策略**
- ✅ 允许修改可变字段（name、description）
- ✅ 核心字段（contract、operation、connection）修改前必须检查引用状态（DS-1）
- ✅ 通过 `updateCoreFields(boolean isReferenced, ...)` 方法，由应用服务层提供引用检查结果

**决策 2：Extension 版本管理**
- ✅ Extension 不需要单独的 version 字段
- ✅ 版本通过 DatasourceId 的 version 来管理
- ✅ Extension 只声明插件 id

**决策 3：OperationSpec**
- ✅ HttpOperationSpec 包含所有和 http 相关的参数，如 query/headers/body/method/path

**决策 4：Option 字段**
- ⏸️ 暂时不实现，待后续需求明确

---

**文档版本**：1.1  
**最后更新**：2024  
**维护者**：Flow 团队

