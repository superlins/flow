package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * 工作流标识符
 * 支持版本号：(key, version) 复合主键
 *
 * @author renc
 */
public final class WorkflowId implements ValueObject<WorkflowId> {

    private final String key;
    private final int version;

    private WorkflowId(String key, int version) {
        Assert.hasText(key, "Workflow key must not be empty");
        Assert.isTrue(version > 0, "Workflow version must be positive");
        this.key = key;
        this.version = version;
    }

    /**
     * 创建新工作流标识符（版本号为1）
     */
    public static WorkflowId of(String key) {
        return new WorkflowId(key, 1);
    }

    /**
     * 从数据库恢复（指定版本号）
     */
    public static WorkflowId of(String key, int version) {
        return new WorkflowId(key, version);
    }

    public String key() {
        return key;
    }

    public int version() {
        return version;
    }

    /**
     * 创建新版本的标识符
     */
    public WorkflowId nextVersion() {
        return new WorkflowId(key, version + 1);
    }

    @Override
    public boolean sameValueAs(WorkflowId other) {
        return other != null
                && Objects.equals(this.key, other.key)
                && this.version == other.version;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof WorkflowId other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, version);
    }

    @Override
    public String toString() {
        return key + ":" + version;
    }
}
