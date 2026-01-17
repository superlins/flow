package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.ValueObject;

import java.util.Objects;

/**
 * 工作流契约
 * 描述工作流的输入输出要求（JSON Schema）
 *
 * @author renc
 */
public final class WorkflowContract implements ValueObject<WorkflowContract> {

    private final String inputSchema;
    private final String outputSchema;

    private WorkflowContract(String inputSchema, String outputSchema) {
        this.inputSchema = inputSchema != null ? inputSchema : "{}";
        this.outputSchema = outputSchema != null ? outputSchema : "{}";
    }

    /**
     * 创建完整契约
     */
    public static WorkflowContract of(String inputSchema, String outputSchema) {
        return new WorkflowContract(inputSchema, outputSchema);
    }

    /**
     * 创建空契约（默认）
     */
    public static WorkflowContract empty() {
        return new WorkflowContract("{}", "{}");
    }

    public String inputSchema() {
        return inputSchema;
    }

    public String outputSchema() {
        return outputSchema;
    }

    @Override
    public boolean sameValueAs(WorkflowContract other) {
        if (other == null) return false;
        return Objects.equals(inputSchema, other.inputSchema) &&
                Objects.equals(outputSchema, other.outputSchema);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowContract that = (WorkflowContract) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputSchema, outputSchema);
    }
}
