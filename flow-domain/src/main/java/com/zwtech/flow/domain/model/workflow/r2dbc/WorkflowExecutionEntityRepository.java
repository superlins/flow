package com.zwtech.flow.domain.model.workflow.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * WorkflowExecution 的 R2DBC 数据库操作接口
 *
 * @author renc
 */
@Repository
public interface WorkflowExecutionEntityRepository extends R2dbcRepository<WorkflowExecutionEntity, Long> {

    @Query("SELECT * FROM FLW_WORKFLOW_EXECUTION WHERE EXECUTION_ID_ = :executionId")
    Mono<WorkflowExecutionEntity> findByExecutionId(String executionId);

    @Query("SELECT * FROM FLW_WORKFLOW_EXECUTION WHERE WORKFLOW_KEY_ = :key AND WORKFLOW_VERSION_ = :version ORDER BY STARTED_AT_ DESC")
    List<WorkflowExecutionEntity> findByWorkflowKeyAndVersion(String key, Integer version);
}
