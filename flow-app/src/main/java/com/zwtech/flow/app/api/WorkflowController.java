package com.zwtech.flow.app.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

/**
 * Simple Workflow REST API
 * Note: This is a simplified version. Production implementation would require proper
 * dependency injection of repositories and services.
 *
 * @author renc
 */
@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    // In-memory storage for demo purposes
    private final Map<String, Map<Integer, Map<String, Object>>> workflowStore = new HashMap<>();
    private final Map<String, Map<String, Object>> executionStore = new HashMap<>();

    /**
     * 创建工作流
     */
    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> createWorkflow(@RequestBody Map<String, Object> request) {
        return Mono.fromCallable(() -> {
            String key = (String) request.get("key");
            String name = (String) request.get("name");
            String description = (String) request.getOrDefault("description", "");
            String status = "DRAFT";
            String inputSchema = (String) request.getOrDefault("inputSchema", "{}");
            String outputSchema = (String) request.getOrDefault("outputSchema", "{}");
            Instant now = Instant.now();

            if (key == null || key.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "key is required"));
            }

            // Create workflow data
            Map<String, Object> workflow = new HashMap<>();
            workflow.put("id", key + ":1");
            workflow.put("key", key);
            workflow.put("version", 1);
            workflow.put("name", name);
            workflow.put("description", description);
            workflow.put("status", status);
            workflow.put("inputSchema", inputSchema);
            workflow.put("outputSchema", outputSchema);
            workflow.put("createdAt", now);
            workflow.put("updatedAt", now);
            workflow.put("nodes", new LinkedHashMap<>());
            workflow.put("connections", new LinkedHashMap<>());

            // Store workflow
            workflowStore.computeIfAbsent(key, k -> new HashMap<>())
                    .put(1, workflow);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(workflow);
        });
    }

    /**
     * 获取工作流详情
     */
    @GetMapping("/{key}/{version}")
    public Mono<ResponseEntity<Map<String, Object>>> getWorkflow(
            @PathVariable String key,
            @PathVariable int version) {

        return Mono.fromCallable(() -> {
            var versions = workflowStore.get(key);
            if (versions == null || !versions.containsKey(version)) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(versions.get(version));
        });
    }

    /**
     * 启用工作流
     */
    @PostMapping("/{key}/{version}/enable")
    public Mono<ResponseEntity<Map<String, Object>>> enableWorkflow(
            @PathVariable String key,
            @PathVariable int version) {

        return Mono.fromCallable(() -> {
            var versions = workflowStore.get(key);
            if (versions == null || !versions.containsKey(version)) {
                return ResponseEntity.notFound().build();
            }

            var workflow = versions.get(version);
            workflow.put("status", "ENABLED");
            workflow.put("updatedAt", Instant.now());

            return ResponseEntity.ok(workflow);
        });
    }

    /**
     * 停用工作流
     */
    @PostMapping("/{key}/{version}/disable")
    public Mono<ResponseEntity<Map<String, Object>>> disableWorkflow(
            @PathVariable String key,
            @PathVariable int version) {

        return Mono.fromCallable(() -> {
            var versions = workflowStore.get(key);
            if (versions == null || !versions.containsKey(version)) {
                return ResponseEntity.notFound().build();
            }

            var workflow = versions.get(version);
            workflow.put("status", "DISABLED");
            workflow.put("updatedAt", Instant.now());

            return ResponseEntity.ok(workflow);
        });
    }

    /**
     * 归档工作流
     */
    @PostMapping("/{key}/{version}/archive")
    public Mono<ResponseEntity<Map<String, Object>>> archiveWorkflow(
            @PathVariable String key,
            @PathVariable int version) {

        return Mono.fromCallable(() -> {
            var versions = workflowStore.get(key);
            if (versions == null || !versions.containsKey(version)) {
                return ResponseEntity.notFound().build();
            }

            var workflow = versions.get(version);
            workflow.put("status", "ARCHIVED");
            workflow.put("updatedAt", Instant.now());

            return ResponseEntity.ok(workflow);
        });
    }

    /**
     * 执行工作流
     */
    @PostMapping("/{key}/{version}/execute")
    public Mono<ResponseEntity<Map<String, Object>>> executeWorkflow(
            @PathVariable String key,
            @PathVariable int version,
            @RequestBody JsonNode input) {

        return Mono.fromCallable(() -> {
            var versions = workflowStore.get(key);
            if (versions == null || !versions.containsKey(version)) {
                return ResponseEntity.notFound().build();
            }

            var workflow = versions.get(version);

            if (!"ENABLED".equals(workflow.get("status"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Workflow is not enabled"));
            }

            // Create execution
            String executionId = UUID.randomUUID().toString();
            Instant now = Instant.now();

            Map<String, Object> execution = new HashMap<>();
            execution.put("executionId", executionId);
            execution.put("workflowId", workflow.get("id"));
            execution.put("status", "SUCCESS");
            execution.put("input", input);
            execution.put("output", Map.of("result", "Workflow executed successfully"));
            execution.put("errorMessage", (String) null);
            execution.put("startedAt", now);
            execution.put("finishedAt", now);
            execution.put("durationMs", 0L);

            // Store execution
            executionStore.put(executionId, execution);

            return ResponseEntity.ok(execution);
        });
    }

    /**
     * 查询工作流列表
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> listWorkflows(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String status) {

        return Mono.fromCallable(() -> {
            List<Map<String, Object>> workflows = new ArrayList<>();

            if (key != null) {
                var versions = workflowStore.get(key);
                if (versions != null) {
                    for (var wf : versions.values()) {
                        if (status == null || status.equals(wf.get("status"))) {
                            workflows.add(wf);
                        }
                    }
                }
            } else {
                for (var versionMap : workflowStore.values()) {
                    for (var wf : versionMap.values()) {
                        if (status == null || status.equals(wf.get("status"))) {
                            workflows.add(wf);
                        }
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("workflows", workflows);
            result.put("total", workflows.size());
            return ResponseEntity.ok(result);
        });
    }

    /**
     * 获取执行详情
     */
    @GetMapping("/executions/{executionId}")
    public Mono<ResponseEntity<Map<String, Object>>> getExecution(
            @PathVariable String executionId) {

        return Mono.fromCallable(() -> {
            var execution = executionStore.get(executionId);
            if (execution == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(execution);
        });
    }
}
