package com.zwtech.flow.domain.model.workflow.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Workflow 的 R2DBC 数据库操作接口
 *
 * @author renc
 */
@Repository
public interface WorkflowEntityRepository extends R2dbcRepository<WorkflowEntity, Long> {

    @Query("SELECT * FROM FLW_WORKFLOW WHERE KEY_ = :key AND VERSION_ = :version")
    Mono<WorkflowEntity> findByKeyAndVersion(String key, Integer version);

    @Query("SELECT * FROM FLW_WORKFLOW WHERE KEY_ = :key ORDER BY VERSION_ DESC")
    Flux<WorkflowEntity> findByKey(String key);

    @Query("SELECT * FROM FLW_WORKFLOW WHERE STATUS_ = :status ORDER BY UPDATED_AT_ DESC")
    Flux<WorkflowEntity> findByStatus(String status);

    @Query("SELECT * FROM FLW_WORKFLOW WHERE KEY_ = :key AND STATUS_ = :status ORDER BY VERSION_ DESC")
    Flux<WorkflowEntity> findByKeyAndStatus(String key, String status);

    @Query("SELECT * FROM FLW_WORKFLOW WHERE STATUS_ = :status OR KEY_ = :key")
    Flux<WorkflowEntity> findByKeyOrStatus(String key, String status);
}
