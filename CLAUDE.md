# CLAUDE.md

## Project Overview

Flow 是 Spring Boot 4.0.1 + Java 25 声明式 API 平台，遵循 DDD 原则，多模块 Maven 架构。

## Module Structure

```
flow-core      # Plugin system (PF4J), SpEL parser, execution context (VariableContext)
flow-domain    # Domain models: ApiDatasource, ApiService, Workflow
flow-app       # REST APIs, Spring configuration, auto-configuration
flow-connector # Connector framework (HTTP, R2DBC) with specs & binding layers
flow-frontend  # React + TypeScript + Tailwind + DaisyUI
```

## API Endpoints

### Workflows (`/api/workflows`)
- `GET /api/workflows` - List workflows
- `POST /api/workflows` - Create workflow
- `GET /api/workflows/{key}/{version}` - Get details
- `POST /api/workflows/{key}/{version}/enable` - Enable/disable/execute

### Datasources (`/api/datasources`)
- `GET /api/datasources` - List datasources
- `POST /api/datasources` - Create datasource
- `GET /api/datasources/{key}/{version}` - Get details
- `POST /api/datasources/{key}/{version}/enable` - Enable/disable

### Services (`/api/services`)
- `GET /api/services` - List services (supports `?datasourceKey=` & `?mode=` filters)
- `POST /api/services` - Create service (DATASOURCE or WORKFLOW mode)
- `GET /api/services/{key}` - Get details
- `POST /api/services/{key}/enable` - Enable/disable
- `PATCH /api/services/{key}` - Update metadata

## Key Architecture

### Flow-Connector Specs Layer

```
ApiDatasource (domain)          Specs (execution)
    │                           ├── getConnection()
    └── SpecsConverter.toSpecs()   ├── getOperation()
                                ├── getInputMappings()
                                └── getOutputMappings()
```

- **DatasourceSpecs** - 可序列化的执行配置，分离领域模型与运行时逻辑
- **HttpDatasourceSpecs** / **R2dbcDatasourceSpecs** - 类型特化实现

### Binding Layer

```
RequestBinder<REQ, SPECS>       ResponseConverter<RESP, SPECS>
    bind(Exchange, SPECS) → REQ      convert(RESP) → JsonNode
                                 project(RESP, SPECS, Context) → JsonNode
```

- **RequestBinder** - 将 ExecutionExchange 映射为 RequestSpec
- **ResponseConverter** - 支持 direct/convert/project 三种转换模式

### Unified VariableContext

```java
VariableContext interface:
    - getRequestAt("user.id")      // 属性路径访问
    - getVariable("timer")          // 动态变量
    - setVariable("result", value)  // 设置变量
    - withResponse(JsonNode)        // 含响应的上下文副本
```

### Filter Chain

- **GlobalFilter** - Spring Bean 注入，@Order 排序
  - `LoggingFilter` - Pre/Post 阶段日志
  - `MetricsFilter` - Micrometer 指标采集
- **ConnectorFilter** - PF4J 插件，按 Order 统一排序

### Spring Auto-Configuration

```java
@Configuration
@ConditionalOnClass(WebClient.class)
public class ConnectorAutoConfiguration {
    @Bean WebClient.Builder webClientBuilder()
    @Bean HttpConnectorFactory httpConnectorFactory()
    @Bean HttpRequestBinder httpRequestBinder()
    @Bean HttpResponseConverter httpResponseConverter()
    @Bean HttpConnectorAdapter httpConnectorAdapter()
}
```

## Database

PostgreSQL via Docker Compose (`docker-compose up postgres -d`):
- Database: `mydatabase`
- User: `myuser`
- Password: `secret`
- Port: 5432

## Testing

```bash
# Run all tests
./mvnw test -Drevision=0.0.1-SNAPSHOT

# Run tests for specific module
./mvnw test -pl flow-core -Drevision=0.0.1-SNAPSHOT
./mvnw test -pl flow-app -Drevision=0.0.1-SNAPSHOT
```

**Test Coverage:**
- flow-core: JsonSchemaTest (2 tests passing)
- flow-app: SpringBootTestApplication (basic integration test)
