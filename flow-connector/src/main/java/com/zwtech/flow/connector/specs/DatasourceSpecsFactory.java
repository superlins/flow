package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;

/**
 * DatasourceSpecs 工厂接口 (SPI)
 * <p>
 * 每种数据源类型需实现此接口，提供从 ApiDatasource 到 DatasourceSpecs 的转换能力。
 * 通过 SPI 机制支持扩展新的数据源类型，无需修改核心代码。
 *
 * @author renc
 */
public interface DatasourceSpecsFactory {

    /**
     * 是否支持该数据源类型
     *
     * @param type 数据源类型名称（如 "HTTP", "R2DBC"）
     * @return 支持返回 true
     */
    boolean supports(String type);

    /**
     * 从 ApiDatasource 创建 DatasourceSpecs
     *
     * @param datasource 领域模型
     * @return 执行规格对象
     */
    DatasourceSpecs create(ApiDatasource datasource);
}
