package com.zwtech.flow.core;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

/**
 * 统一变量上下文接口
 * <p>
 * 提供表达式解析所需的变量访问能力，支持：
 * - 请求和响应数据
 * - 动态变量存储
 * - 嵌套属性访问
 *
 * @author renc
 */
public interface VariableContext {

    /**
     * 获取请求数据
     */
    Optional<JsonNode> getRequest();

    /**
     * 获取响应数据
     */
    Optional<JsonNode> getResponse();

    /**
     * 获取指定路径的请求数据
     * 支持 JsonPath 风格的路径，如 "user.id"
     */
    Optional<JsonNode> getRequestAt(String path);

    /**
     * 获取指定路径的响应数据
     * 支持 JsonPath 风格的路径，如 "data.items[0].name"
     */
    Optional<JsonNode> getResponseAt(String path);

    /**
     * 获取变量值
     */
    <T> Optional<T> getVariable(String name, Class<T> type);

    /**
     * 获取变量值，如果不存在返回默认值
     */
    <T> T getVariableOrDefault(String name, Class<T> type, T defaultValue);

    /**
     * 设置变量值
     */
    <T> void setVariable(String name, T value);

    /**
     * 获取所有变量
     */
    Map<String, Object> getVariables();

    /**
     * 创建子上下文（用于并行执行等场景）
     */
    VariableContext createChild();

    /**
     * 创建包含新响应的上下文副本
     */
    VariableContext withResponse(JsonNode response);
}
