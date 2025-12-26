n8n 是一个开源的低代码自动化工作流工具，其核心是一个基于节点（Node）和边（Edge）的流程引擎，用于执行可编排的工作流（Workflow）。其流程引擎的设计围绕“节点执行顺序”、“数据流传递”、“错误处理”和“可扩展性”展开。

以下是基于对 [n8n 官方 GitHub 仓库](https://github.com/n8n-io/n8n)（截至 2024 年末最新稳定版）源码的分析，对流程引擎核心设计的详细说明：

---

## 一、整体架构概览

n8n 的流程引擎主要由以下模块组成：

- **Workflow**：代表一个完整的流程图（DAG，有向无环图）。
- **Node**：流程中的单个操作单元（如 HTTP 请求、数据库写入等）。
- **Connection**：定义节点之间的连接关系（输入/输出）。
- **WorkflowExecute**：负责调度与执行整个 Workflow。
- **NodeExecuteFunctions**：为节点提供运行时上下文（如获取输入数据、发送 HTTP 请求等）。
- **IWorkflowExecuteAdditionalData**：提供执行环境（如凭证、日志、Webhook 等）。

执行入口通常为 `WorkflowExecute.run()`。

---

## 二、核心数据结构

### 1. Workflow（流程定义）

```ts
interface IWorkflowBase {
  nodes: INode[];              // 所有节点
  connections: IConnections;  // 节点之间的连接
}

interface INode {
  id: string;
  name: string;
  type: string;               // 节点类型，如 'n8n-nodes-base.httpRequest'
  typeVersion: number;
  position: [number, number];
  parameters: IDataObject;    // 节点配置参数
}
```

### 2. Connections（连接关系）

```ts
interface IConnections {
  [nodeName: string]: {
    [inputType: string]: IConnection[][];
  };
}

interface IConnection {
  node: string;     // 目标节点名
  type: string;     // 连接类型（main、辅助等）
  index: number;    // 输入/输出索引
}
```

> 例如：`Start` 节点输出连接到 `HTTP Request` 节点的主输入。

### 3. 执行数据（Execution Data）

n8n 使用 `IRunExecutionData` 表示执行过程中的状态：

```ts
interface IRunExecutionData {
  startData?: IRunStartData;
  resultData: {
    runData: IRunData;        // 每个节点的执行结果
    lastNodeExecuted?: string;
  };
  executionData?: {
    contextData: IContextObject;
    nodeExecutionStack: IExecutingWorkflowData[]; // 待执行节点栈
    waitingExecution: IWaitingForExecution;       // 等待中的分支（用于分支/并行）
  };
}
```

- `runData[nodeName]`：存储该节点执行后输出的 JSON 数据（数组形式，支持多条数据流）。
- 数据格式为 `IExecuteResponsePromiseData[]`，本质是 `INodeExecutionData[]`。

### 4. 节点输入/输出数据格式

```ts
interface INodeExecutionData {
  json: IDataObject;        // 主体数据
  binary?: IBinaryKeyData;  // 二进制数据（如文件）
  pairedItem?: IPairedItem; // 用于追踪数据来源（用于调试/错误定位）
}
```

> 所有数据在节点间以数组形式传递：`INodeExecutionData[]`。

---

## 三、执行引擎核心逻辑（伪代码）

执行流程由 `WorkflowExecute.run()` 启动，其核心伪代码如下：

```python
function run(workflow: IWorkflowBase, inputData: INodeExecutionData[]):

    # 1. 初始化执行上下文
    runExecutionData = {
        resultData: { runData: {} },
        executionData: {
            nodeExecutionStack: [],
            waitingExecution: {}
        }
    }

    # 2. 找到所有起始节点（无输入或手动触发）
    startNodes = findStartNodes(workflow)

    # 3. 将起始节点加入执行栈
    for node in startNodes:
        pushToExecutionStack(node, inputData)

    # 4. 主循环：执行节点直到栈空
    while executionStack is not empty:

        currentNode = popFromExecutionStack()

        # 获取该节点的输入数据（来自上游节点）
        inputData = collectInputData(currentNode, runExecutionData.resultData.runData)

        try:
            # 执行节点逻辑（调用节点实现类的 execute 方法）
            outputData = await executeNode(currentNode, inputData, workflow, additionalData)

            # 保存结果
            runExecutionData.resultData.runData[currentNode.name] = outputData

            # 5. 将下游节点加入执行栈（支持多输出分支）
            for each outputConnection of currentNode:
                targetNode = outputConnection.node
                if targetNode not in executed and not in stack:
                    pushToExecutionStack(targetNode, outputData[outputConnection.index])

        except error:
            handleError(currentNode, error, runExecutionData)

    # 6. 返回完整执行结果
    return runExecutionData
```

### 关键点说明：

1. **数据流驱动**：每个节点的输出作为下游节点的输入，数据以数组形式流动（支持一对多、多对一）。
2. **并行/分支支持**：通过 `waitingExecution` 处理条件分支（如 IF 节点）或并行执行。
3. **错误传播**：节点抛出错误时，可通过 `onError` 配置决定是否继续执行（如重试、跳过、停止）。
4. **执行上下文隔离**：每个节点执行时获得 `NodeExecuteFunctions` 提供的工具函数（如 `.getInputData()`, `.helpers.httpRequest()`）。

---

## 四、节点执行机制

每个节点类型（如 `HttpRequest`）必须实现 `INode` 接口，并提供 `execute` 方法：

```ts
class HttpRequest implements INodeType {
  description = { ... };

  async execute(this: IExecuteFunctions): Promise<INodeExecutionData[][]> {
    const items = this.getInputData(); // 获取输入数据（INodeExecutionData[]）
    const returnData: INodeExecutionData[] = [];

    for (const item of items) {
      const response = await this.helpers.httpRequest({
        method: 'GET',
        url: item.json.url as string,
      });

      returnData.push({
        json: { ...item.json, response },
        pairedItem: { item: item.index } // 保留数据来源
      });
    }

    // 返回二维数组：[[分支0输出], [分支1输出], ...]
    return [returnData];
  }
}
```

> **注意**：返回类型是 `INodeExecutionData[][]`，因为一个节点可以有多个输出端口（如 true/false 分支）。

---

## 五、执行顺序与拓扑排序

n8n 在执行前会进行**拓扑排序**（Topological Sort）以确定节点执行顺序（避免循环依赖）：

- 通过 `workflow.connections` 构建 DAG。
- 使用 Kahn 算法或 DFS 检测环并排序。
- 实际执行时仍依赖动态调度（因为某些节点可能条件跳过）。

---

## 六、扩展性设计

1. **节点插件化**：所有节点通过 `NodeHelpers` 注册，支持社区自定义节点。
2. **Credentials（凭证）系统**：敏感信息（API Key）通过加密凭证注入节点。
3. **Webhook 支持**：部分节点可注册 webhook，暂停流程等待外部回调（通过 `waitingExecution` 挂起）。

---

## 七、总结：n8n 流程引擎设计特点

| 特性           | 说明                                |
|--------------|-----------------------------------|
| **数据流驱动**    | 所有节点输入/输出为 `INodeExecutionData[]` |
| **DAG 执行模型** | 支持复杂连接、分支、合并                      |
| **动态调度**     | 不完全依赖静态拓扑，支持运行时决策（如 IF/Loop）      |
| **错误隔离与恢复**  | 每个节点可独立处理错误                       |
| **高度可扩展**    | 节点、凭证、函数均可插件化                     |

---

## 参考源码路径（n8n 仓库）

- 执行引擎：`/packages/workflow/src/WorkflowExecute.ts`
- 节点接口：`/packages/workflow/src/Interfaces.ts`
- 节点基类：`/packages/nodes-base/nodes/...`
- 连接与数据流：`/packages/workflow/src/WorkflowDataProxy.ts`

> 注：以上分析基于 n8n v1.x 架构，具体实现可能随版本演进微调，但核心设计保持稳定。

如需深入某一部分（如错误处理机制、Webhook 挂起恢复、二进制数据处理），可进一步展开。