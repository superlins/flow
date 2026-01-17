package com.zwtech.flow.domain.model.workflow.r2dbc;

import com.zwtech.flow.domain.model.workflow.*;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Workflow R2DBC Repository 实现
 *
 * 职责：在领域模型与数据库模型之间翻译语义
 *
 * @author renc
 */
@Repository
public class R2dbcWorkflowRepository implements WorkflowRepository {

    private final WorkflowEntityRepository workflowEntityRepository;

    public R2dbcWorkflowRepository(WorkflowEntityRepository workflowEntityRepository) {
        this.workflowEntityRepository = workflowEntityRepository;
    }

    @Override
    public Mono<Workflow> save(Workflow workflow) {
        WorkflowEntity entity = WorkflowEntity.fromWorkflow(workflow);
        return workflowEntityRepository.save(entity)
                .map(WorkflowEntity::toWorkflow);
    }

    @Override
    public Mono<Workflow> findById(String key, int version) {
        return workflowEntityRepository.findByKeyAndVersion(key, version)
                .map(WorkflowEntity::toWorkflow);
    }

    @Override
    public Mono<List<Workflow>> findByKey(String key) {
        return workflowEntityRepository.findByKey(key)
                .map(WorkflowEntity::toWorkflow)
                .collect(Collectors.toList());
    }

    @Override
    public Mono<List<Workflow>> findByKey(String key, WorkflowStatus status) {
        return workflowEntityRepository.findByKeyAndStatus(key, status.name())
                .map(WorkflowEntity::toWorkflow)
                .collect(Collectors.toList());
    }

    @Override
    public Mono<Void> delete(String key, int version) {
        return workflowEntityRepository.findByKeyAndVersion(key, version)
                .flatMap(workflowEntityRepository::delete);
    }
}
