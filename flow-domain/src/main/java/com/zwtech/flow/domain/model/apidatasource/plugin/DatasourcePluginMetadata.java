package com.zwtech.flow.domain.model.apidatasource.plugin;

import com.zwtech.flow.domain.model.apidatasource.DatasourceType;

import java.util.Map;
import java.util.Objects;

/**
 * DatasourcePlugin 元数据
 * <p>
 * 描述插件的基本信息和能力
 *
 * @author renc
 */
public class DatasourcePluginMetadata {

    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final String author;
    private final java.util.List<DatasourceType> supportedTypes;
    private final Map<String, String> configSchema;
    private final boolean requiresAuth;
    private final DatasourceType supportedType; // 单一类型（用于简化）

    public DatasourcePluginMetadata(
            String id,
            String name,
            String version,
            String description,
            DatasourceType supportedType) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.version = version != null ? version : "1.0.0";
        this.description = description != null ? description : "";
        this.author = "Unknown";
        this.supportedTypes = supportedType != null ? java.util.List.of(supportedType) : java.util.List.of();
        this.configSchema = Map.of();
        this.requiresAuth = false;
        this.supportedType = supportedType;
    }

    public DatasourcePluginMetadata(
            String id,
            String name,
            String version,
            String description,
            String author,
            java.util.List<DatasourceType> supportedTypes,
            Map<String, String> configSchema,
            boolean requiresAuth) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.version = Objects.requireNonNull(version, "version must not be null");
        this.description = description != null ? description : "";
        this.author = author != null ? author : "Unknown";
        this.supportedTypes = java.util.List.copyOf(Objects.requireNonNull(supportedTypes, "supportedTypes must not be null"));
        this.configSchema = configSchema != null ? Map.copyOf(configSchema) : Map.of();
        this.requiresAuth = requiresAuth;
        this.supportedType = supportedTypes.isEmpty() ? null : supportedTypes.get(0);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String description() {
        return description;
    }

    public String author() {
        return author;
    }

    public java.util.List<DatasourceType> supportedTypes() {
        return supportedTypes;
    }

    public Map<String, String> configSchema() {
        return configSchema;
    }

    public boolean requiresAuth() {
        return requiresAuth;
    }

    public DatasourceType supportedType() {
        return supportedType;
    }

    // Alias methods for compatibility
    public String pluginId() {
        return id;
    }

    public String pluginName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasourcePluginMetadata that = (DatasourcePluginMetadata) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DatasourcePluginMetadata{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", supportedTypes=" + supportedTypes +
                ", requiresAuth=" + requiresAuth +
                '}';
    }

    /**
     * Builder pattern for creating metadata
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String version;
        private String description;
        private String author;
        private java.util.List<DatasourceType> supportedTypes;
        private Map<String, String> configSchema;
        private boolean requiresAuth = false;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder supportedTypes(java.util.List<DatasourceType> supportedTypes) {
            this.supportedTypes = supportedTypes;
            return this;
        }

        public Builder configSchema(Map<String, String> configSchema) {
            this.configSchema = configSchema;
            return this;
        }

        public Builder requiresAuth(boolean requiresAuth) {
            this.requiresAuth = requiresAuth;
            return this;
        }

        public DatasourcePluginMetadata build() {
            return new DatasourcePluginMetadata(
                    id, name, version, description, author,
                    supportedTypes, configSchema, requiresAuth);
        }
    }
}