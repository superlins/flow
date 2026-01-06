package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * 扩展声明
 * 
 * Datasource 只声明依赖的扩展插件，不包含插件配置。
 * 插件配置存储在插件自己的存储中，执行期由引擎统一装配。
 * 
 * Extension 的版本通过 DatasourceId 的 version 来管理，不需要单独的 version 字段。
 *
 * @author renc
 */
public final class Extension implements ValueObject<Extension> {
    
    private final String id;

    public Extension(String id) {
        Assert.hasText(id, "Extension id must not be empty");
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public boolean sameValueAs(Extension other) {
        return other != null && id.equals(other.id);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Extension other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Extension{id='" + id + "'}";
    }
}
