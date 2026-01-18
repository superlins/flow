package com.zwtech.flow.connector.binding;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.connector.ResponseSpec;
import com.zwtech.flow.connector.specs.DatasourceSpecs;
import com.zwtech.flow.core.VariableContext;

import java.util.Map;

/**
 * 响应转换器接口，将 ResponseSpec 转换为 JsonNode
 * <p>
 * 每个 Connector 类型需要实现自己的 ResponseConverter。
 * 支持两种转换模式：
 * <ol>
 *   <li>直接转换：ResponseSpec → JsonNode（用于基础的数据提取）</li>
 *   <li>投影转换：ResponseSpec + Mappings → JsonNode（用于字段映射）</li>
 * </ol>
 *
 * @param <RESP extends ResponseSpec> ResponseSpec 类型
 * @author renc
 */
public interface ResponseConverter<RESP extends ResponseSpec, SPECS extends DatasourceSpecs> {

    /**
     * 将 ResponseSpec 转换为 JsonNode（直接转换）
     * <p>
     * 此方法提取 ResponseSpec 的核心数据，不考虑字段映射。
     * 用于构建变量上下文中的响应变量。
     *
     * @param response ResponseSpec
     * @return JsonNode
     */
    JsonNode convert(RESP response);

    /**
     * 将 ResponseSpec 投影为目标 JsonNode（字段映射）
     * <p>
     * 根据 mapping 配置，从 ResponseSpec 中提取字段并映射到目标结构。
     * 支持使用 VariableContext 进行表达式解析。
     *
     * @param response    ResponseSpec
     * @param specs       数据源规格，包含映射配置
     * @param context     变量上下文，用于表达式解析
     * @return 投影后的 JsonNode
     */
    JsonNode project(RESP response, DatasourceSpecs specs, VariableContext context);

    /**
     * 将 ResponseSpec 转换为用于变量上下文的 JsonNode
     * <p>
     * 此方法将完整的响应数据（包括状态、头部、体）转换为统一的变量格式，
     * 供表达式解析使用。
     *
     * @param response ResponseSpec
     * @return 统一格式的 JsonNode（包含 status, headers, body 等）
     */
    JsonNode toVariableFormat(RESP response);
}
