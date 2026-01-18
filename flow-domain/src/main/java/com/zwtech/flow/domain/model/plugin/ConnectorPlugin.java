package com.zwtech.flow.domain.model.plugin;

import com.zwtech.flow.domain.shared.ValueObject;
import org.pf4j.ExtensionPoint;

import java.util.*;

/**
 * ConnectorPlugin 扩展点声明
 * <p>
 * 这是插件提供的 ConnectorFilter 扩展点的声明式定义。
 * 实际的 ConnectorFilter 实现在插件 JAR 的 extension points 中。
 * <p>
 * 例如：
 * <pre>
 * @Extension
 * public class MySecurityFilter implements ConnectorFilter {
 *     ...
 * }
 * </pre>
 * <p>
 * 领域模型中只记录这个扩展点的元数据：
 * - extensionClass: com.example.MySecurityFilter
 * - order: 1000（优先级）
 * - datasourceTypes: [HTTP, R2DBC]（适用的数据源类型）
 *
 * @author renc
 */
public record ConnectorPlugin(
    /**
     * 扩展类全限定名
     * 例如：com.example.MySecurityFilter
     */
    String extensionClass,

    /**
     * 扩展点优先级（数字越小越先执行）
     * 系统内置过滤器使用 0-999，用户插件使用 1000+ 范围
     */
    int order,

    /**
     * 插件类型
     */
    String pluginType,

    /**
     * 配置参数（JSON 序列化后）
     */
    Map<String, Object> config,

    /**
     * 此扩展适用的数据源类型
     * 为空表示适用于所有数据源类型
     */
    List<String> applicableDatasourceTypes
) implements ValueObject<ConnectorPlugin> {

    public ConnectorPlugin {
        Objects.requireNonNull(extensionClass, "extensionClass must not be null");
        pluginType = pluginType != null ? pluginType : "UNKNOWN";
        config = config != null ? Map.copyOf(config) : Map.of();
        applicableDatasourceTypes = applicableDatasourceTypes != null
                ? List.copyOf(applicableDatasourceTypes)
                : List.of();
    }

    public static ConnectorPlugin of(
            String extensionClass,
            int order,
            String pluginType
    ) {
        return new ConnectorPlugin(extensionClass, order, pluginType, Map.of(), List.of());
    }

    @Override
    public boolean sameValueAs(ConnectorPlugin other) {
        if (other == null) return false;
        return extensionClass.equals(other.extensionClass);
    }

    /**
     * 构造器 Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String extensionClass;
        private int order;
        private String pluginType;
        private Map<String, Object> config = Map.of();
        private List<String> applicableDatasourceTypes = List.of();

        public Builder extensionClass(String val) {
            this.extensionClass = val;
            return this;
        }

        public Builder order(int val) {
            this.order = val;
            return this;
        }

        public Builder pluginType(String val) {
            this.pluginType = val;
            return this;
        }

        public Builder config(Map<String, Object> val) {
            this.config = val;
            return this;
        }

        public Builder applicableDatasourceTypes(List<String> val) {
            this.applicableDatasourceTypes = val;
            return this;
        }

        public Builder addApplicableDatasourceType(String type) {
            if (this.applicableDatasourceTypes.isEmpty()) {
                this.applicableDatasourceTypes = new ArrayList<>();
            }
            this.applicableDatasourceTypes.add(type);
            return this;
        }

        public ConnectorPlugin build() {
            return new ConnectorPlugin(
                    extensionClass,
                    order,
                    pluginType,
                    config,
                    applicableDatasourceTypes
            );
        }
    }
}
