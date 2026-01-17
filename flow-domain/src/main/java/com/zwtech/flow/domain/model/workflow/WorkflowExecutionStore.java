package com.zwtech.flow.domain.model.workflow;

import reactor.core.publisher.Mono;

/**
 * WorkflowExecution 存储接口
 * 用于持久化执行状态，支持取消操作
 *
 * @author renc
 */
public interface WorkflowExecutionStore {

    /**
     * 获取执行实例
     */
    Mono<WorkflowExecution> getExecution(WorkflowExecutionId executionId);

    /**
     * 保存执行实例
     */
    Mono<Void> saveExecution(WorkflowExecution execution);

    /**
     * 空实现：不持久化，支持取消操作但数据不保存
     */
    WorkflowExecutionStore NULL = new WorkflowExecutionStore() {
        @Override
        public Mono<WorkflowExecution> getExecution(WorkflowExecutionId executionId) {
            return Mono.error(new UnsupportedOperationException("Execution store not configured"));
        }

        @Override
        public Mono<Void> saveExecution(WorkflowExecution execution) {
            return Mono.empty();
        }
    };
}
