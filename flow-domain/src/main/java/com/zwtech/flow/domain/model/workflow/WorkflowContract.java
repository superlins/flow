package com.zwtech.flow.domain.model.workflow;

import com.zwtech.flow.domain.shared.Contract;

/**
 * 工作流契约
 * <p>
 * 继承自 {@link Contract}，描述工作流的输入输出要求（JSON Schema）。
 * 支持空契约作为默认值。
 *
 * @author renc
 */
public final class WorkflowContract extends Contract {

    private WorkflowContract(String inputSchema, String outputSchema) {
        super(inputSchema != null && !inputSchema.isEmpty() ? inputSchema : "{}",
                outputSchema != null && !outputSchema.isEmpty() ? outputSchema : "{}");
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

    @Override
    public boolean sameValueAs(Contract other) {
        if (!super.sameValueAs(other)) {
            return false;
        }
        return other instanceof WorkflowContract;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        WorkflowContract that = (WorkflowContract) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
