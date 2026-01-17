package com.zwtech.flow.app.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zwtech.flow.domain.model.workflow.*;
import com.zwtech.flow.domain.service.WorkflowExecutionService;
import com.zwtech.flow.domain.model.workflow.WorkflowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Workflow REST API with Persistence
 * Integrates WorkflowRepository and WorkflowExecutionService for real workflow execution
 *
 * @author renc
 */
@RestController
@RequestMapping("/api/workflows")
public class PersistentWorkflowController {

    private static final Logger logger = LoggerFactory.getLogger(PersistentWorkflowController.class);

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionService executionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PersistentWorkflowController(
            WorkflowRepository workflowRepository,
            WorkflowExecutionService executionService) {
        this.workflowRepository = workflowRepository;
        this.executionService = executionService;
    }

    /**
     * 创建工作流
     */
    @PostMapping
    public Mono<ResponseEntity<Workflow>> createWorkflow(@RequestBody Map<String, Object> request) {
        return Mono.fromCallable(() -> {
            String key = (String) request.get("key");
            String name = (String) request.get("name");
            String description = (String) request.getOrDefault("description", "");

            if (key == null || key.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key is required");
            }
            if (name == null || name.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
            }

            // 创建新的工作流（版本从 1 开始）
            var workflowId = WorkflowId.of(key, 1);
            var workflow = Workflow.create(workflowId, name, description, WorkflowContract.empty());

            return workflow;
        }).flatMap(workflowRepository::save)
                .map(workflow -> ResponseEntity.status(HttpStatus.CREATED).body(workflow));
    }

    /**
     * 获取工作流详情
     */
    @GetMapping("/{key}/{version}")
    public Mono<ResponseEntity<Workflow>> getWorkflow(
            @PathVariable String key,
            @PathVariable int version) {

        return workflowRepository.findById(key, version)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(ResponseEntity::ok);
    }

    /**
     * 启用工作流
     */
    @PostMapping("/{key}/{version}/enable")
    public Mono<ResponseEntity<Workflow>> enableWorkflow(
            @PathVariable String key,
            @PathVariable int version) {

        return workflowRepository.findById(key, version)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(workflow -> {
                    try {
                        workflow.enable();
                        return workflowRepository.save(workflow)
                                .map(ResponseEntity::ok);
                    } catch (IllegalStateException e) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
                    }
                });
    }

    /**
     * 停用工作流
     */
    @PostMapping("/{key}/{version}/disable")
    public Mono<ResponseEntity<Workflow>> disableWorkflow(
            @PathVariable String key,
            @PathVariable int version) {

        return workflowRepository.findById(key, version)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(workflow -> {
                    try {
                        workflow.disable();
                        return workflowRepository.save(workflow)
                                .map(ResponseEntity::ok);
                    } catch (IllegalStateException e) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
                    }
                });
    }

    /**
     * 归档工作流
     */
    @PostMapping("/{key}/{version}/archive")
    public Mono<ResponseEntity<Workflow>> archiveWorkflow(
            @PathVariable String key,
            @PathVariable int version) {

        return workflowRepository.findById(key, version)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(workflow -> {
                    try {
                        workflow.archive();
                        return workflowRepository.save(workflow)
                                .map(ResponseEntity::ok);
                    } catch (IllegalStateException e) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage()));
                    }
                });
    }

    /**
     * 执行工作流
     */
    @PostMapping("/{key}/{version}/execute")
    public Mono<ResponseEntity<WorkflowExecution>> executeWorkflow(
            @PathVariable String key,
            @PathVariable int version,
            @RequestBody JsonNode input) {

        return workflowRepository.findById(key, version)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(workflow -> {
                    if (workflow.status() != WorkflowStatus.ENABLED) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Workflow is not enabled, current status: " + workflow.status()));
                    }

                    var executionId = WorkflowExecutionId.of(UUID.randomUUID().toString());
                    return executionService.execute(workflow, executionId, input)
                            .map(ResponseEntity::ok)
                            .onErrorResume(e -> {
                                logger.error("Failed to execute workflow", e);
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Execution failed: " + e.getMessage()));
                            });
                });
    }

    /**
     * 查询工作流列表
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> listWorkflows(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) WorkflowStatus status) {

        Mono<List<Workflow>> workflowsMono;

        if (key != null) {
            if (status != null) {
                workflowsMono = workflowRepository.findByKey(key, status);
            } else {
                workflowsMono = workflowRepository.findByKey(key);
            }
        } else {
            // 如果没有指定 key，需要查询所有工作流
            WorkflowStatus queryStatus = status;
            workflowsMono = Mono.fromCallable(() -> List.<Workflow>of())
                    .flatMap(list -> Mono.error(new UnsupportedOperationException(
                            "Querying all workflows by status is not yet supported. Please specify a key parameter.")));
        }

        return workflowsMono
                .map(workflows -> {
                    Map<String, Object> result = Map.of(
                            "workflows", workflows,
                            "total", workflows.size()
                    );
                    return ResponseEntity.ok(result);
                });
    }

    /**
     * 获取执行详情
     */
    @GetMapping("/executions/{executionId}")
    public Mono<ResponseEntity<WorkflowExecution>> getExecution(
            @PathVariable String executionId) {

        return executionService.getExecution(WorkflowExecutionId.of(executionId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(ResponseEntity::ok);
    }

    /**
     * 取消执行
     */
    @PostMapping("/executions/{executionId}/cancel")
    public Mono<ResponseEntity<WorkflowExecution>> cancelExecution(
            @PathVariable String executionId) {

        return executionService.cancel(WorkflowExecutionId.of(executionId))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    logger.error("Failed to cancel execution", e);
                    return Mono.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Cancel failed: " + e.getMessage()));
                });
    }
}
