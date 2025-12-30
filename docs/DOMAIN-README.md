## Domain Driver Design - Domain Models

<!-- TOC -->
  * [Domain Driver Design - Domain Models](#domain-driver-design---domain-models)
    * [ApiService](#apiservice)
    * [ApiDatasource](#apidatasource)
    * [ApiWorkflow](#apiworkflow)
<!-- TOC -->

### ApiService

对外暴露 http 端点，处理用户请求/响应，schema 验证，对内引用一个 ApiDatasource 或一个 ApiWorkflow

```json
{
  "id": "uuid",
  "name": "A online api-service",
  "category": "DATASOURCE|WORKFLOW",
  "input": {
    "type": "object",
    "properties": {
      "name": {
        "type": "string",
        "description": "user name"
      },
      "phone": {
        "type": "string",
        "description": "user phone"
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
  "description": ""
}
```

### ApiDatasource

常见的 HTTP、RPC、JDBC、NoSQL 等 “契约模型” 抽象，“统一抽象 + 类型特化”；连接池、超时、重试、认证、限流；可测试、可验证

> 将数据源执行过程可引入插件（Kong Plugins）
> ApiServiceRequest -> [预处理 -> 协议适配 -> 后处理]-> ApiServiceResponse

- HTTP

遵循 OpenAPI 风格

> POST

```json
{
  "id": "ds-http-post-create-order",
  "name": "Create Order (HTTP POST)",
  "type": "http",
  "version": "1",
  "description": "DataSource for creating orders",
  "specification": {
    "url": "https://api.example.com/v2/orders",
    "path": "/orders",
    "method": "POST",
    "inputSchema": {
      "type": "object",
      "properties": {
        "X-Internal-Token": {
          "type": "string",
          "description": "User token",
          "x-in": "header"
        },
        "userId": {
          "type": "string",
          "description": "User ID",
          "x-in": "body"
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
                "x-internal": true
                // 关键标记：内部常量
              },
              "uid": {
                "type": "string",
                "default": "user1",
                "x-internal": true
                // 关键标记：默认值
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
    // http config
    "connectionTimeout": "PT5S",
    "responseTimeout": "PT10S",
    "retryDisabled": false,
    "compressionEnabled": false,
    "certVerifyDisabled": false,
    "timeoutMs": 5000,
    // retry config
    "retry": {
      "maxAttempts": 1
    },
    // rate limiter config
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

### ApiWorkflow
