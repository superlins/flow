package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * 工作流执行标识符
 * 使用 UUID v7 或类似时间排序的 UUID 生成
 *
 * @author renc
 */
public final class WorkflowExecutionId implements ValueObject<WorkflowExecutionId> {

    private final String value;

    private WorkflowExecutionId(String value) {
        Assert.hasText(value, "WorkflowExecutionId value must not be empty");
        this.value = value;
    }

    /**
     * 创建新的执行 ID（使用 UUID）
     */
    public static WorkflowExecutionId of() {
        return new WorkflowExecutionId(java.util.UUID.randomUUID().toString());
    }

    /**
     * 从字符串恢复
     */
    public static WorkflowExecutionId of(String value) {
        return new WorkflowExecutionId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean sameValueAs(WorkflowExecutionId other) {
        return other != null && this.value.equals(other.value);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof WorkflowExecutionId other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
