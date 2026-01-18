package com.zwtech.flow.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.zwtech.flow.domain.model.workflow.*;
import com.zwtech.flow.domain.service.DatasourceExecutionService;
import com.zwtech.flow.domain.service.WorkflowExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 工作流执行服务默认实现（应用层）
 * 负责协调 Workflow 的执行过程，处理 DAG 执行、并行分支、节点调度等核心逻辑
 *
 * @author renc
 */
@Service
public class DefaultWorkflowExecutionService implements WorkflowExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWorkflowExecutionService.class);

    private final DatasourceExecutionService datasourceExecutionService;

    public DefaultWorkflowExecutionService(DatasourceExecutionService datasourceExecutionService) {
        this.datasourceExecutionService = datasourceExecutionService;
    }

    @Override
    public WorkflowExecution createExecution(Workflow workflow, WorkflowExecutionId executionId, JsonNode input) {
        var execution = WorkflowExecution.create(executionId, workflow.id(), input);
        execution.start(); // 标记为运行中
        return execution;
    }

    @Override
    public Mono<WorkflowExecution> execute(Workflow workflow, WorkflowExecutionId executionId, JsonNode input) {
        return execute(workflow, executionId, input, Duration.ofMinutes(30)); // 默认30分钟超时
    }

    @Override
    public Mono<WorkflowExecution> execute(Workflow workflow, WorkflowExecutionId executionId, JsonNode input, Duration timeout) {
        var execution = createExecution(workflow, executionId, input);

        return executeInternal(workflow, execution)
                .timeout(timeout)
                .onErrorResume(throwable -> {
                    if (throwable instanceof java.util.concurrent.TimeoutException) {
                        return Mono.fromCallable(() -> {
                            execution.timeout();
                            return execution;
                        });
                    }
                    return Mono.fromCallable(() -> {
                        execution.fail(throwable.getMessage());
                        return execution;
                    });
                });
    }

    private Mono<WorkflowExecution> executeInternal(Workflow workflow, WorkflowExecution execution) {
        // 1. 找到所有起始节点（入度为0的节点）
        var startNodes = findStartNodes(workflow.nodes(), workflow.connections());

        if (startNodes.isEmpty()) {
            return Mono.fromCallable(() -> {
                execution.fail("No start nodes found in workflow");
                return execution;
            });
        }

        // 2. 执行 DAG
        return executeDag(workflow, execution, startNodes);
    }

    private Mono<WorkflowExecution> executeDag(Workflow workflow, WorkflowExecution execution, Map<String, Node> startNodes) {
        // 上下文： nodeId -> {output, dependents}
        var context = new ExecutionContext(workflow);

        // 执行起始节点
        return executeNodes(workflow, execution, context, new java.util.ArrayList<>(startNodes.values()))
                .flatMap(executed -> {
                    // 检查是否所有节点都完成了
                    if (allNodesCompleted(workflow, context)) {
                        return Mono.fromCallable(() -> {
                            // 获取终止节点的输出（出度为0的节点）
                            var output = getTerminationNodeOutput(workflow, context);
                            execution.complete(output);
                            return execution;
                        });
                    } else {
                        // 继续执行有所有前置节点都完成的节点
                        return continueExecution(workflow, execution, context);
                    }
                });
    }

    private Mono<WorkflowExecution> executeNodes(
            Workflow workflow,
            WorkflowExecution execution,
            ExecutionContext context,
            List<Node> nodes) {

        if (nodes.isEmpty()) {
            return Mono.just(execution);
        }

        // 简化实现：顺序执行节点，后续可优化为并行
        var result = new java.util.ArrayList<Node>();
        Mono<WorkflowExecution> resultMono = Mono.just(execution);

        for (var node : nodes) {
            resultMono = resultMono.flatMap(exec -> {
                return executeNode(workflow, execution, context, node)
                        .doOnNext(n -> {
                            result.add(n);
                            execution.markNodeCompleted(n.id(), context.getNodeOutput(n.id()));
                        })
                        .thenReturn(exec);
            });
        }

        return resultMono;
    }

    private Mono<Node> executeNode(
            Workflow workflow,
            WorkflowExecution execution,
            ExecutionContext context,
            Node node) {

        execution.markNodeStarted(node.id());

        if (node.type() == Node.NodeType.DATASOURCE) {
            // 执行 Datasource 节点
            return executeDatasourceNode(workflow, execution, context, node);
        } else {
            // 执行 Simple 节点
            return executeSimpleNode(workflow, execution, context, node);
        }
    }

    private Mono<Node> executeDatasourceNode(
            Workflow workflow,
            WorkflowExecution execution,
            ExecutionContext context,
            Node node) {

        var nodeInput = aggregateNodeInputs(workflow, context, node);
        var datasourceId = node.datasourceId();
        var operationKey = node.operationKey();

        // 从节点配置中获取 contract
        var contract = getDatasourceContract(node);

        // 调用 DatasourceExecutionService 执行操作
        return datasourceExecutionService.execute(datasourceId, operationKey, nodeInput, contract)
                .map(output -> {
                    context.setNodeOutput(node.id(), output);
                    return node;
                })
                .doOnError(error -> {
                    logger.error("Failed to execute datasource node: {}, error: {}", node.id(), error.getMessage());
                });
    }

    /**
     * 从节点配置中提取 DatasourceContract
     */
    private com.zwtech.flow.domain.model.apidatasource.DatasourceContract getDatasourceContract(Node node) {
        var config = node.config();
        Object inputSchema = config.get("inputSchema");
        Object outputSchema = config.get("outputSchema");
        Object strict = config.get("strict");

        String inputSchemaStr = inputSchema != null ? inputSchema.toString() : "{}";
        String outputSchemaStr = outputSchema != null ? outputSchema.toString() : "{}";
        boolean strictBool = strict != null && Boolean.parseBoolean(strict.toString());

        return new com.zwtech.flow.domain.model.apidatasource.DatasourceContract(inputSchemaStr, outputSchemaStr, strictBool);
    }

    private Mono<Node> executeSimpleNode(
            Workflow workflow,
            WorkflowExecution execution,
            ExecutionContext context,
            Node node) {

        var nodeInput = aggregateNodeInputs(workflow, context, node);

        // Simple 节点：返回输入作为输出（占位实现）
        // 实际应用中可以根据 node.config() 实现不同的行为
        return Mono.fromCallable(() -> {
            context.setNodeOutput(node.id(), nodeInput);
            return node;
        });
    }

    /**
     * 聚合节点输入，根据连接配置进行字段映射
     */
    private JsonNode aggregateNodeInputs(Workflow workflow, ExecutionContext context, Node targetNode) {
        // 找到所有指向当前节点的连接
        var incomingConnections = workflow.connections().values().stream()
                .filter(conn -> conn.targetNodeId().equals(targetNode.id()))
                .collect(Collectors.toList());

        var result = context.getObjectMapper().createObjectNode();
        var objectMapper = context.getObjectMapper();

        for (var conn : incomingConnections) {
            var sourceOutput = context.getNodeOutput(conn.sourceNodeId());
            if (sourceOutput == null || !sourceOutput.isObject()) {
                continue;
            }

            var sourceOutputField = conn.sourceOutputField();
            var targetInputField = conn.targetInputField();

            // 从源输出中提取字段值
            JsonNode fieldValue;
            if (sourceOutputField == null || sourceOutputField.isEmpty() || sourceOutputField.equals("output")) {
                // 如果没有指定字段或字段为 "output"，使用整个输出
                fieldValue = sourceOutput;
            } else {
                // 按路径提取字段值（支持点号分隔的路径，如 "data.user.id"）
                fieldValue = extractFieldByPath(sourceOutput, sourceOutputField, objectMapper);
            }

            // 将值映射到目标字段
            if (targetInputField == null || targetInputField.isEmpty() || targetInputField.equals("input")) {
                // 如果没有指定目标字段或字段为 "input"，合并整个对象
                mergeJsonNodes(result, fieldValue, objectMapper);
            } else {
                // 按路径设置字段值
                setFieldByPath(result, targetInputField, fieldValue, objectMapper);
            }
        }

        return result;
    }

    /**
     * 按路径从 JsonNode 中提取字段值
     */
    private JsonNode extractFieldByPath(JsonNode node, String path, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        if (node == null || path == null || path.isEmpty()) {
            return objectMapper.nullNode();
        }

        var parts = path.split("\\.");
        JsonNode current = node;

        for (var part : parts) {
            if (current == null || current.isNull()) {
                return objectMapper.nullNode();
            }

            if (current.isArray()) {
                try {
                    var index = Integer.parseInt(part);
                    if (index >= 0 && index < current.size()) {
                        current = current.get(index);
                    } else {
                        return objectMapper.nullNode();
                    }
                } catch (NumberFormatException e) {
                    return objectMapper.nullNode();
                }
            } else {
                if (current.isObject() && current.has(part)) {
                    current = current.get(part);
                } else {
                    return objectMapper.nullNode();
                }
            }
        }

        return current;
    }

    /**
     * 按路径设置 JsonNode 的字段值
     */
    private void setFieldByPath(com.fasterxml.jackson.databind.node.ObjectNode target, String path,
                                JsonNode value, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        if (target == null || path == null || path.isEmpty()) {
            return;
        }

        var parts = path.split("\\.");
        com.fasterxml.jackson.databind.node.ObjectNode current = target;

        // 遍历路径，最后一个部分是目标字段
        for (int i = 0; i < parts.length - 1; i++) {
            var part = parts[i];
            if (!current.has(part)) {
                current.set(part, objectMapper.createObjectNode());
            }
            var nextNode = current.get(part);
            if (!nextNode.isObject()) {
                // 如果中间路径不为对象，重新创建
                current.set(part, objectMapper.createObjectNode());
                nextNode = current.get(part);
            }
            current = (com.fasterxml.jackson.databind.node.ObjectNode) nextNode;
        }

        // 设置最后一个字段的值
        current.set(parts[parts.length - 1], value);
    }

    /**
     * 合并两个 JsonNode
     */
    private void mergeJsonNodes(com.fasterxml.jackson.databind.node.ObjectNode target, JsonNode source,
                               com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        if (source == null || source.isNull()) {
            return;
        }

        if (source.isObject()) {
            var fields = source.fields();
            while (fields.hasNext()) {
                var entry = fields.next();
                var key = entry.getKey();
                var value = entry.getValue();

                if (target.has(key) && target.get(key).isObject() && value.isObject()) {
                    // 递归合并对象
                    mergeJsonNodes((com.fasterxml.jackson.databind.node.ObjectNode) target.get(key), value, objectMapper);
                } else {
                    // 直接覆盖
                    target.set(key, value);
                }
            }
        } else {
            // 如果 source 不是对象，作为 "value" 字段放入
            target.set("value", source);
        }
    }

    private Mono<WorkflowExecution> processDependents(
            Workflow workflow,
            WorkflowExecution execution,
            ExecutionContext context,
            List<Node> completedNodes) {

        // 对于每个完成的节点，检查可以执行的下游节点
        var readyToExecute = new HashSet<Node>();

        for (var node : completedNodes) {
            var dependentNodes = findDependentNodes(workflow, node.id());
            for (var dependent : dependentNodes) {
                if (canExecuteNode(workflow, context, dependent.id())) {
                    readyToExecute.add(dependent);
                }
            }
        }

        if (readyToExecute.isEmpty()) {
            return Mono.just(execution);
        }

        // 执行就绪的节点
        return executeNodes(workflow, execution, context, List.copyOf(readyToExecute));
    }

    private Mono<WorkflowExecution> continueExecution(
            Workflow workflow,
            WorkflowExecution execution,
            ExecutionContext context) {

        // 找到可以执行的节点（所有前置节点都已完成）
        var readyNodes = workflow.nodes().values().stream()
                .filter(node -> !context.isNodeCompleted(node.id()))
                .filter(node -> canExecuteNode(workflow, context, node.id()))
                .collect(Collectors.toList());

        if (readyNodes.isEmpty()) {
            // 没有就绪节点，可能遇到了循环依赖或者其他问题
            return Mono.fromCallable(() -> {
                execution.fail("No ready nodes found - possible circular dependency");
                return execution;
            });
        }

        return executeNodes(workflow, execution, context, readyNodes)
                .flatMap(result -> processDependents(workflow, execution, context, readyNodes));
    }

    private List<Node> findDependentNodes(Workflow workflow, String nodeId) {
        return workflow.connections().values().stream()
                .filter(conn -> conn.sourceNodeId().equals(nodeId))
                .map(conn -> workflow.nodes().get(conn.targetNodeId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean canExecuteNode(Workflow workflow, ExecutionContext context, String nodeId) {
        // 找到所有前置节点
        var predecessors = workflow.connections().values().stream()
                .filter(conn -> conn.targetNodeId().equals(nodeId))
                .map(Connection::sourceNodeId)
                .collect(Collectors.toSet());

        // 如果没有前置节点（起始节点），可以执行
        if (predecessors.isEmpty()) {
            return !context.isNodeCompleted(nodeId); // 还没执行过
        }

        // 所有前置节点都必须已完成
        for (var predId : predecessors) {
            if (!context.isNodeCompleted(predId)) {
                return false;
            }
        }

        return !context.isNodeCompleted(nodeId); // 还没执行过
    }

    private Map<String, Node> findStartNodes(Map<String, Node> nodes, Map<String, Connection> connections) {
        Set<String> hasIncoming = new HashSet<>();
        for (var conn : connections.values()) {
            hasIncoming.add(conn.targetNodeId());
        }

        return nodes.entrySet().stream()
                .filter(entry -> !hasIncoming.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean allNodesCompleted(Workflow workflow, ExecutionContext context) {
        return workflow.nodes().keySet().stream()
                .allMatch(context::isNodeCompleted);
    }

    private JsonNode getTerminationNodeOutput(Workflow workflow, ExecutionContext context) {
        // 找到终止节点（出度为0的节点）
        var terminationNodes = findTerminationNodes(workflow);

        if (terminationNodes.isEmpty()) {
            return context.getObjectMapper().createObjectNode();
        }

        // 如果有多个终止节点，将它们的输出合并
        var result = context.getObjectMapper().createObjectNode();
        for (var node : terminationNodes) {
            var output = context.getNodeOutput(node.id());
            if (output != null) {
                result.set(node.id(), output);
            }
        }

        return result;
    }

    private List<Node> findTerminationNodes(Workflow workflow) {
        Set<String> hasOutgoing = new HashSet<>();
        for (var conn : workflow.connections().values()) {
            hasOutgoing.add(conn.sourceNodeId());
        }

        return workflow.nodes().values().stream()
                .filter(node -> !hasOutgoing.contains(node.id()))
                .collect(Collectors.toList());
    }

    @Override
    public Mono<WorkflowExecution> cancel(WorkflowExecutionId executionId) {
        // TODO: 实现取消逻辑
        return Mono.error(new UnsupportedOperationException("Cancel not implemented yet"));
    }

    @Override
    public Mono<WorkflowExecution> getExecution(WorkflowExecutionId executionId) {
        // TODO: 从存储中获取执行实例
        return Mono.error(new UnsupportedOperationException("GetExecution not implemented yet"));
    }

    /**
     * 执行上下文：存储节点输出和完成状态
     */
    private static class ExecutionContext {
        private final Workflow workflow;
        private final Map<String, JsonNode> nodeOutputs = new HashMap<>();
        private final Set<String> completedNodes = new HashSet<>();

        ExecutionContext(Workflow workflow) {
            this.workflow = workflow;
        }

        public JsonNode getNodeOutput(String nodeId) {
            return nodeOutputs.get(nodeId);
        }

        public void setNodeOutput(String nodeId, JsonNode output) {
            nodeOutputs.put(nodeId, output);
            completedNodes.add(nodeId);
        }

        public boolean isNodeCompleted(String nodeId) {
            return completedNodes.contains(nodeId);
        }

        public com.fasterxml.jackson.databind.ObjectMapper getObjectMapper() {
            return new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }
}
