package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.shared.DomainException;

/**
 * Schema 验证领域服务
 *
 * 职责：根据 JSON Schema 验证输入数据
 *
 * 设计意图：
 * - Schema 的解析与校验逻辑属于领域模型的一部分
 * - 是否满足 Schema = 是否允许执行
 * - 不满足 = 业务失败，不是技术异常
 *
 * @author renc
 */
public interface SchemaValidationService {

    /**
     * 根据 JSON Schema 验证输入数据
     *
     * @param schema JSON Schema 字符串
     * @param input 待验证的输入数据
     * @throws DomainException 如果验证失败
     */
    void validate(String schema, Object input);
}