package com.zwtech.flow.domain.model.workflow;

/**
 * 工作流执行状态
 *
 * @author renc
 */
public enum WorkflowExecutionStatus {
    /**
     * 等待中：等待队列中
     */
    PENDING,

    /**
     * 运行中：正在执行
     */
    RUNNING,

    /**
     * 已成功：所有节点执行成功
     */
    SUCCESS,

    /**
     * 已失败：至少一个节点执行失败
     */
    FAILED,

    /**
     * 已取消：用户主动取消
     */
    CANCELED,

    /**
     * 超时：执行超时未完成
     */
    TIMEOUT
}
