# Flow Domain Module

**flow-domain** is the heart of the Flow system. It implements the core business logic, rules, and state transitions using **Domain-Driven Design (DDD)** principles. It is designed to be framework-agnostic (mostly) and centers around the ubiquotous language of the business.

## 🏗️ Architecture & Design

The module is structured around **Aggregates**, **Domain Services**, and a **Shared Kernel**.

### 1. Domain Model (Aggregates)

| Aggregate | Package | Responsibilities | Key Components |
|-----------|---------|------------------|----------------|
| **ApiDatasource** | `model.apidatasource` | Manages external API/Database connections and definitions. | `DatasourceContract`, `DatasourceOperation`, `DatasourceConnection` |
| **Workflow** | `model.workflow` | Orchestrates business logic execution flows using a node-based graph. | `Workflow`, `WorkflowExecution`, `Node`, `Connection` |
| **ApiService** | `model.apiservice` | Encapsulates business capabilities exposed to consumers. | `ServiceContract`, `BindingSpec`, `FieldBinding` |
| **Plugin** | `model.plugin` | Manages dynamic extensions and script executions. | `PluginId`, `ScriptLanguage`, `PluginContent` |

### 2. Shared Kernel (`domain.shared`)

Contains common building blocks used across aggregates:
-   **DDD Primitives**: `AggregateRoot`, `ValueObject`, `DomainEvent`.
-   **Specifications**: `AbstractSpecification`, `AndSpecification` for complex rule validation.
-   **Mapping Engine**: A recursive, type-safe engine for data transformation (`MappingNode`).

### 3. Domain Services (`domain.service`)

Stateless services that coordinate cross-aggregate logic:
-   `DatasourceExecutionService`: Executes datasource operations.
-   `WorkflowExecutionService`: Manages lifecycle of workflow runs.
-   `SchemaValidationService`: Validates JSON schemas for contracts.
-   `PluginRegistry`: Manages plugin loading and discovery.

---

## 🌟 Key Features

### Recursive Mapping Engine
The `ApiDatasource` uses a powerful `MappingSpec` to handle complex, nested JSON structures from third-party APIs.

-   **ExpressionNode**: SpEL expressions for dynamic values (`{{ #input.id }}`).
-   **ObjectNode**: Nested JSON objects.
-   **ArrayNode**: Static lists or dynamic loops (iterating over collections).

### Workflow Orchestration
The workflow engine supports:
-   **Versioning**: Workflows are versioned (e.g., `v1`, `v2`).
-   **Execution Tracking**: Full audit trail of execution status (`STARTED`, `COMPLETED`, `FAILED`).
-   **Node Graph**: Flexible `Node` and `Connection` model to build DAGs.

---

## 🚀 Usage Guidelines

### 1. Defining an API Datasource

```java
// 1. Define the Contract (Input/Output Schemas)
var contract = new DatasourceContract(inputSchema, outputSchema);

// 2. Define the HTTP Operation with Mappings
var operation = HttpDatasourceOperation.withMappings(
    "https://api.example.com/orders",
    "POST",
    MappingSpec.empty(), // Headers
    MappingSpec.empty(), // Query Params
    MappingSpec.ofObject(Map.of( // Body
        "userId", new ExpressionNode("{{ #input.user_id }}")
    )),
    MappingSpec.ofObject(Map.of( // Response
        "orderId", new ExpressionNode("{{ #resp.id }}")
    ))
);

// 3. Create & Enable Aggregate
var datasource = ApiDatasource.create(new DatasourceId("order-api", 1))
    .configure(DatasourceType.HTTP, "Order API", "Creates orders", contract, operation, connection, List.of())
    .enable();
```

### 2. Creating a Workflow

```java
// 1. Create Workflow
var workflow = Workflow.create(new WorkflowId("order-process"), "Order Processing Flow");

// 2. Add Nodes
var startNode = new Node("start", "Start", NodeType.START);
var apiNode = new Node("call-api", "Call Order API", NodeType.TASK);
workflow.addNode(startNode);
workflow.addNode(apiNode);

// 3. Connect Nodes
workflow.connect(new Connection(startNode.getId(), apiNode.getId()));

// 4. Publish
workflow.publish();
```

---

## ⚠️ Technical Debt & Roadmap

While the domain model is robust, there are known architectural debts being addressed:

### 1. Infrastructure Leakage (Resolved)
The R2DBC implementations have been successfully moved to the `flow-core` code module (acting as the infrastructure layer), adhering to clean architecture principles.
-   **Moved Packages**:
    -   `model.apidatasource.r2dbc` -> `infrastructure.repository.apidatasource`
    -   `model.workflow.r2dbc` -> `infrastructure.repository.workflow`
    -   `model.apiservice.r2dbc` -> `infrastructure.repository.apiservice`

### 2. Serialization Support
The `MappingNode` sealed interface hierarchy needs complete Jackson annotations (`@JsonTypeInfo`) to ensure correct polymorphic serialization/deserialization for persistence and API transport.

---

## 🧪 Testing

The domain module should be tested in isolation using unit tests that verify business rules and aggregate state transitions.

```bash
# Run domain tests
./mvnw test -pl flow-domain
```
