package com.zwtech.flow.domain.model.workflow.r2dbc;

import com.zwtech.flow.domain.model.workflow.WorkflowExecution;
import com.zwtech.flow.domain.model.workflow.WorkflowExecutionId;
import com.zwtech.flow.domain.model.workflow.WorkflowExecutionStore;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * WorkflowExecutionStore 的 R2DBC 实现
 *
 * @author renc
 */
@Repository
public class R2dbcWorkflowExecutionStore implements WorkflowExecutionStore {

    private final WorkflowExecutionEntityRepository entityRepository;

    public R2dbcWorkflowExecutionStore(WorkflowExecutionEntityRepository entityRepository) {
        this.entityRepository = entityRepository;
    }

    @Override
    public Mono<WorkflowExecution> getExecution(WorkflowExecutionId executionId) {
        return entityRepository.findByExecutionId(executionId.value())
                .map(WorkflowExecutionEntity::toWorkflowExecution);
    }

    @Override
    public Mono<Void> saveExecution(WorkflowExecution execution) {
        return entityRepository.save(WorkflowExecutionEntity.fromWorkflowExecution(execution))
                .then();
    }
}
