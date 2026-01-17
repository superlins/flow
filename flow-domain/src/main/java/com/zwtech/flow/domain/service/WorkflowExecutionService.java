package com.zwtech.flow.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.domain.model.workflow.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 工作流执行领域服务接口
 * 负责协调 Workflow 的执行过程，处理 DAG 执行、并行分支、节点调度等核心逻辑
 *
 * @author renc
 */
public interface WorkflowExecutionService {

    /**
     * 执行工作流
     * 同步阻塞执行，返回执行结果
     *
     * @param workflow 要执行的工作流
     * @param executionId 执行标识
     * @param input 输入数据
     * @return WorkflowExecution 执行结果
     */
    Mono<WorkflowExecution> execute(Workflow workflow, WorkflowExecutionId executionId, JsonNode input);

    /**
     * 执行工作流（带超时控制）
     *
     * @param workflow 要执行的工作流
     * @param executionId 执行标识
     * @param input 输入数据
     * @param timeout 超时时间
     * @return WorkflowExecution 执行结果
     */
    Mono<WorkflowExecution> execute(Workflow workflow, WorkflowExecutionId executionId, JsonNode input, Duration timeout);

    /**
     * 创建执行实例但不立即执行
     *
     * @param workflow 工作流
     * @param executionId 执行标识
     * @param input 输入数据
     * @return WorkflowExecution 执行实例
     */
    WorkflowExecution createExecution(Workflow workflow, WorkflowExecutionId executionId, JsonNode input);

    /**
     * 取消执行
     *
     * @param executionId 执行标识
     * @return WorkflowExecution 已取消的执行实例
     */
    Mono<WorkflowExecution> cancel(WorkflowExecutionId executionId);

    /**
     * 获取执行实例
     *
     * @param executionId 执行标识
     * @return WorkflowExecution 执行实例
     */
    Mono<WorkflowExecution> getExecution(WorkflowExecutionId executionId);
}
