package com.zwtech.flow.connector;

import com.zwtech.flow.core.ExecutionExchange;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 数据绑定与转换工具
 *
 * 设计意图：
 * - 负责在 ExecutionExchange / JsonNode 与各类 RequestSpec / ResponseSpec 之间做转换
 * - 在转换前后触发 JSON Schema 校验（由上层传入 schema）
 *
 * 当前实现仅为占位，后续需要集成 Jackson + JSON Schema 校验库
 *
 * @author renc
 */
public abstract class DataBinder {

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * 根据给定的 schema 与 data，将数据绑定为目标类型
     *
     * @param schema JSON Schema
     * @param data   原始数据
     * @param c      目标类型
     * @return 目标类型实例
     */
    public static <T> T bind(JsonNode schema, JsonNode data, Class<T> c) {
        if (data == null) {
            throw new IllegalStateException("input data is null, cannot bind to " + c.getSimpleName());
        }
        // TODO(renc): 使用 JSON Schema 校验 data
        return OBJECT_MAPPER.convertValue(data, c);
    }

    /**
     * 将 JsonNode 直接绑定为目标类型
     *
     * @param n JsonNode
     * @param c 目标类型
     * @return 目标类型实例
     */
    public static <T> T bind(JsonNode n, Class<T> c) {
        if (n == null) {
            throw new IllegalStateException("input json is null, cannot bind to " + c.getSimpleName());
        }
        return OBJECT_MAPPER.convertValue(n, c);
    }

    /**
     * 从 ExecutionExchange 中提取输入数据，并绑定为目标类型
     *
     * 约定：
     * - 使用 ExecutionContext.input() 作为源数据
     * - schema 由调用方在绑定前通过领域服务完成校验
     *
     * @param exchange 执行交换对象
     * @param c        目标类型
     * @return 目标类型实例
     */
    public static <T> T bind(ExecutionExchange exchange, Class<T> c) {
        JsonNode input = exchange.getRequest();
        return bind(input, c);
    }
}

