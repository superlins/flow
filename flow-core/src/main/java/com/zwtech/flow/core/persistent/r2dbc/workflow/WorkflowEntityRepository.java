package com.zwtech.flow.core.persistent.r2dbc.workflow;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Workflow 的 R2DBC 数据库操作接口
 *
 * @author renc
 */
@Repository
public interface WorkflowEntityRepository extends R2dbcRepository<WorkflowEntity, Long> {

    @Query("SELECT * FROM flw_workflow WHERE key_ = :key AND version_ = :version")
    Mono<WorkflowEntity> findByKeyAndVersion(String key, Integer version);

    @Query("SELECT * FROM flw_workflow WHERE key_ = :key ORDER BY version_ DESC")
    Flux<WorkflowEntity> findByKey(String key);

    @Query("SELECT * FROM flw_workflow WHERE status_ = :status ORDER BY updated_at_ DESC")
    Flux<WorkflowEntity> findByStatus(String status);

    @Query("SELECT * FROM flw_workflow WHERE key_ = :key AND status_ = :status ORDER BY version_ DESC")
    Flux<WorkflowEntity> findByKeyAndStatus(String key, String status);

    @Query("SELECT * FROM flw_workflow ORDER BY updated_at_ DESC")
    Flux<WorkflowEntity> findAllOrdered();

    @Query("SELECT * FROM flw_workflow WHERE status_ = :status OR key_ = :key ORDER BY updated_at_ DESC")
    Flux<WorkflowEntity> findByKeyOrStatus(String key, String status);
}
