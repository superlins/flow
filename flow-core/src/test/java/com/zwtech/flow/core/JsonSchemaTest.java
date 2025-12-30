package com.zwtech.flow.core;

import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * @author renc
 */
class JsonSchemaTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    String schema1 = """
            {
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
                    "enum": [ "md5", "sha256", "sm3" ]
                  }
                },
                "required": [
                  "name", "phone", "idcardno"
                ]
              }
            """;

    String schema2 = """
            {
                "info": {
                  "title": "DataSource: Get User",
                  "version": "1.0",
                  "description": "DataSource for user profile lookup"
                },
                "servers": [
                  {
                    "url": "https://api.example.com/v2",
                    "description": "Production server"
                  }
                ],
                "paths": [
                  {
                    "/users/{userId}": {
                      "get": {
                        "summary": "Fetch user profile by ID",
                        "operationId": "getUserProfile",
                        "parameters": [
                          {
                            "name": "userId",
                            "in": "path",
                            "required": true,
                            "schema": {
                              "type": "string"
                            },
                            "description": "User unique ID"
                          },
                          {
                            "name": "includeOrders",
                            "in": "query",
                            "required": false,
                            "schema": {
                              "type": "boolean",
                              "default": false
                            },
                            "description": "Whether to include order history"
                          },
                          {
                            "name": "apikey",
                            "in": "query",
                            "required": true,
                            "schema": {
                              "type": "string",
                              "default": "{{secrets.thirdparty.api_key}}"
                            },
                            "description": "Whether to include order history",
                            "x-internal": true
                          }
                        ],
                        "responses": {
                          "200": {
                            "description": "User profile retrieved successfully",
                            "content": {
                              "application/json": {
                                "schema": {
                                  "type": "object",
                                  "properties": {
                                    "id": {
                                      "type": "string"
                                    },
                                    "name": {
                                      "type": "string"
                                    },
                                    "email": {
                                      "type": "string",
                                      "format": "email"
                                    },
                                    "orders": {
                                      "type": "array",
                                      "items": {
                                        "type": "object",
                                        "properties": {
                                          "orderId": {
                                            "type": "string"
                                          },
                                          "amount": {
                                            "type": "number"
                                          }
                                        }
                                      }
                                    }
                                  }
                                }
                              }
                            }
                          },
                          "404": {
                            "description": "User not found"
                          }
                        }
                      }
                    }
                  }
                ]
              }
            """;

    @Test
    public void testJsonSchema() {
        var schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
        var schema = schemaRegistry.getSchema(schema1);
        var errors = schema.validate("""
                {
                    "name": "test",
                    "phone": "11",
                    "idcardno": "11"
                }
                """, InputFormat.JSON, executionContext -> {
            /*
             * By default since Draft 2019-09 the format keyword only generates annotations
             * and not assertions.
             */
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });
        System.out.println(errors);
    }

    @Test
    public void testOpenApi() {
        var schemaRegistry = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        var schema = schemaRegistry.getSchema(schema2);
        var errors = schema.validate("""
                {
                
                }
                """, InputFormat.JSON);
        System.out.println(errors);
    }
}