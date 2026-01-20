package com.zwtech.flow.connector.specs;

import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DatasourceSpecs 注册表
 * <p>
 * 统一管理所有 DatasourceSpecsFactory，提供根据类型创建 Specs 的能力。
 * 替代原有的 SpecsConverter 静态工具类，支持通过 Spring DI 扩展新类型。
 *
 * @author renc
 */
@Component
public class DatasourceSpecsRegistry {

    private final List<DatasourceSpecsFactory> factories;

    public DatasourceSpecsRegistry(List<DatasourceSpecsFactory> factories) {
        this.factories = factories;
    }

    /**
     * 根据 ApiDatasource 创建对应的 DatasourceSpecs
     *
     * @param datasource 领域模型
     * @return 执行规格对象
     * @throws IllegalArgumentException 如果找不到对应的工厂
     */
    public DatasourceSpecs createSpecs(ApiDatasource datasource) {
        String type = datasource.type().name();
        return factories.stream()
                .filter(f -> f.supports(type))
                .findFirst()
                .map(f -> f.create(datasource))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported datasource type: " + type +
                                ". Available factories support: " + getSupportedTypes()));
    }

    /**
     * 获取支持的数据源类型列表（用于错误提示）
     */
    private String getSupportedTypes() {
        return factories.stream()
                .map(f -> f.getClass().getSimpleName())
                .reduce((a, b) -> a + ", " + b)
                .orElse("none");
    }
}
