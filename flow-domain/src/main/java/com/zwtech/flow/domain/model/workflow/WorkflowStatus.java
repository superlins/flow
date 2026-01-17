package com.zwtech.flow.domain.model.workflow;

/**
 * 工作流状态
 *
 * @author renc
 */
public enum WorkflowStatus {
    /**
     * 编辑中：节点和连接正在配置中
     */
    DRAFT,

    /**
     * 已启用：可以被执行
     */
    ENABLED,

    /**
     * 已停用：暂停使用，不允许执行
     */
    DISABLED,

    /**
     * 已归档：只读状态，不允许修改或执行
     */
    ARCHIVED
}
