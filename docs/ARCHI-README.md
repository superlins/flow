# Archi

<!-- TOC -->
* [Archi](#archi)
  * [Component](#component)
    * [ApiService](#apiservice)
    * [ApiDatasource](#apidatasource)
      * [ExecutionExchange](#executionexchange)
      * [ExecutionEnvelope](#executionenvelope)
      * [RequestSpec](#requestspec)
      * [ResponseSpec](#responsespec)
      * [Connector](#connector)
      * [ConnectorFactory](#connectorfactory)
      * [ConnectorFilter](#connectorfilter)
      * [ConnectorFilterChain](#connectorfilterchain)
  * [Domain Model](#domain-model)
    * [ApiService](#apiservice-1)
    * [ApiDatasource](#apidatasource-1)
<!-- TOC -->

## Component

```text
ServerHttpRequest
+ ServiceInputSchema
   ↓
DataBinder
   ↓
Service.ExecutionContext.input
+ Service.DerivedContext
+ DatasourceInputSchema
   ↓
DataBinder
   ↓
Datasource.ExecutionContext.input
   ↓ (ConnectorAdapter)
xxRequestSpec
   ↓
ExecutionEnvelope
   ↓
FilterChain
   ↓
Connector
   ↓ (ConnectorAdapter)
xxResponseSpec
+ DatasourceOutputSchema
   ↓
DataBinder
   ↓
Datasource.ExecutionContext.output
+ Datasource.DerivedContext
   ↓
Service.ExecutionContext.output
```

### ApiService

```java
public interface ExecutionContext {
    Optional<JsonNode> input();
    Optional<JsonNode> output();
    Optional<DerivedContext> derived();
}

public interface DerivedContext {
    JsonNode get(String path);
    boolean contains(String path);
    DerivedContext with(String path, JsonNode value);
}
```

### ApiDatasource

#### ExecutionExchange

不可变上下文，是 “执行事实世界”，不参与执行管道

```java
public interface ExecutionExchange {
    ExecutionContext context();
    ExecutionExchange mutate(UnaryOperator<ExecutionContext> operator);
    <T> Optional<T> getAttribute(ExchangeAttributeKey<T> key);
    ExecutionExchange withAttribute(ExchangeAttributeKey<?> key, Object value);
}
```

#### ExecutionEnvelope

不可变上下文， 是 “执行进行时世界”，提供 filter 数据支持

```java
public interface ExecutionEnvelope<REQ, RESP> {
    REQ requestSpec();
    ExecutionEnvelope<REQ, RESP> withRequestSpec(REQ request);
    Optional<RESP> responseSpec();
    ExecutionEnvelope<REQ, RESP> withResponseSpec(RESP response);
    ExecutionAttributes attributes();
}
```

#### RequestSpec

不可变的请求对象，connector 统一输入模型

#### ResponseSpec

不可变得的响应对象，connector 统一输出模型

#### Connector

数据源连接器抽象，轻量、无状态

#### ConnectorFactory

数据源连接器抽象工程，适配连接器的客户端缓存

#### ConnectorFilter

数据源过滤器抽象

```java
public interface ConnectorFilter<REQ, RESP> {
    Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope, ConnectorFilterChain chain);
}
```

#### ConnectorFilterChain

```java
public interface ConnectorFilterChain {
    <REQ, RESP> Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope);
}
```

## Domain Model

### ApiService

一个逻辑服务单元，处理用户请求/响应并进行 schema 验证，对内引用一个 ApiDatasource

```json
{
  "id": "",
  "name": "A online api-service",
  "datasource": "ds-http-post-create-order",
  "enabled": true,
  "input": {
    "type": "object",
    "properties": {
      "name": {
        "type": "string",
        "description": "user name"
      },
      "phone": {
        "type": "string",
        "description": "user phone",
        "x-internal-map": "mobile"
      },
      "idcardno": {
        "type": "string",
        "description": "user idcardno"
      },
      "alg": {
        "type": "string",
        "description": "params algorithm",
        "default": "md5",
        "enum": [
          "md5",
          "sha256",
          "sm3"
        ]
      }
    },
    "required": [
      "name",
      "phone",
      "idcardno"
    ]
  },
  "output": {
    "type": "object",
    "properties": {
      "score1": {
        "type": "integer",
        "description": "user score1"
      },
      "score2": {
        "type": "string",
        "description": "user score2"
      }
    }
  },
  "description": "",
  "tags": [
    "post"
  ]
}
```

### ApiDatasource

常见的 HTTP、RPC、JDBC、NoSQL 等 “契约模型” 抽象，“统一抽象 + 类型特化”；连接池、超时、重试、认证、限流；可测试、可验证

- HTTP

> POST

```json
{
  "id": "ds-http-post-create-order",
  "name": "Create Order (HTTP POST)",
  "type": "http",
  "version": "1",
  "description": "DataSource for creating orders",
  "specification": {
    "url": "https://api.example.com/v2",
    "path": "/orders",
    "method": "POST",
    "inputSchema": {
      "type": "object",
      "properties": {
        "X-Internal-Token": {
          "type": "string",
          "description": "User token",
          "x-internal-in": "header"
        },
        "userId": {
          "type": "string",
          "description": "User ID",
          "x-internal-in": "body"
        },
        "items": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "productId": {
                "type": "string"
              },
              "quantity": {
                "type": "integer",
                "minimum": 1
              },
              "secret": {
                "type": "string",
                "const": "apikey",
                "x-internal-const": "apikey"
              },
              "uid": {
                "type": "string",
                "default": "user1",
                "x-internal-default": "user1"
              }
            },
            "required": [
              "productId",
              "quantity"
            ]
          }
        }
      },
      "required": [
        "userId",
        "items"
      ]
    },
    "outputSchema": {
      "type": "object",
      "properties": {
        "orderId": {
          "type": "string"
        },
        "status": {
          "type": "string",
          "enum": [
            "pending",
            "confirmed"
          ]
        },
        "totalAmount": {
          "type": "number"
        }
      }
    }
  },
  "connection": {
    "connectionTimeout": "PT5S",
    "responseTimeout": "PT10S",
    "retryDisabled": false,
    "compressionEnabled": false,
    "certVerifyDisabled": false,
    "timeoutMs": 5000,
    "retry": {
      "maxAttempts": 1
    },
    "rateLimiter": {
    }
  },
  "extension": [
    {
      "id": "oauth2-enricher"
    },
    {
      "id": "logging"
    }
  ],
  "tags": [
    "post"
  ]
}
```

- R2DBC

遵循 SQL + JsonSchema 参数绑定风格

```json
{
  "id": "ds-mysql-user-orders",
  "name": "Get User Orders from MySQL",
  "type": "mysql",
  "version": "1",
  "specification": {
    "sql": "SELECT order_id AS orderId, product_name AS product, amount, created_at AS createdAt FROM orders WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit",
    "inputSchema": {
      "type": "object",
      "properties": {
        "userId": {
          "type": "string"
        },
        "limit": {
          "type": "integer",
          "default": 10,
          "maximum": 100
        }
      },
      "required": [
        "userId"
      ]
    },
    "outputSchema": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "orderId": {
            "type": "string"
          },
          "product": {
            "type": "string"
          },
          "amount": {
            "type": "number"
          },
          "createdAt": {
            "type": "string",
            "format": "date-time"
          }
        }
      }
    }
  },
  "connection": {
    "host": "prod-mysql.cluster-xxx.us-east-1.rds.amazonaws.com",
    "port": 3306,
    "database": "order_db",
    "username": "{{secrets.mysql_order_reader_user}}",
    "password": "{{secrets.mysql_order_reader_pwd}}",
    "ssl": true,
    "poolSize": 5
  }
}
```

- Redis

遵循 Command + JsonSchema 参数绑定风格

```json
{
  "id": "ds-redis-user-session",
  "name": "Get User Session from Redis",
  "type": "redis",
  "version": "1",
  "specification": {
    "command": "HGETALL session:{{sessionId}}",
    "inputSchema": {
      "type": "object",
      "properties": {
        "sessionId": {
          "type": "string"
        }
      },
      "required": [
        "sessionId"
      ]
    },
    "outputSchema": {
      "type": "object",
      "properties": {
        "userId": {
          "type": "string"
        },
        "loginTime": {
          "type": "string",
          "format": "date-time"
        },
        "permissions": {
          "type": "array",
          "items": {
            "type": "string"
          }
        }
      }
    }
  },
  "connection": {
    "host": "redis-prod.example.com",
    "port": 6379,
    "password": "{{secrets.redis_auth}}",
    "useSsl": true,
    "timeoutMs": 2000
  }
}
```

