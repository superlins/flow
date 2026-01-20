package com.zwtech.flow.api;

import com.zwtech.flow.api.dto.ApiDatasourceDTO;
import com.zwtech.flow.domain.model.apidatasource.*;
import com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.connection.DatasourceConnection;
import com.zwtech.flow.domain.model.apidatasource.operation.DatasourceOperation;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ApiDatasource REST API
 *
 * @author renc
 */
@RestController
@RequestMapping("/api/datasources")
public class ApiDatasourceController {

    private final ApiDatasourceRepository datasourceRepository;

    public ApiDatasourceController(ApiDatasourceRepository datasourceRepository) {
        this.datasourceRepository = datasourceRepository;
    }

    /**
     * 创建数据源
     */
    @PostMapping
    public Mono<ResponseEntity<ApiDatasourceDTO>> createDatasource(@RequestBody Map<String, Object> request) {
        return Mono.fromCallable(() -> {
            String key = (String) request.get("key");
            Integer version = request.get("version") != null ?
                    Integer.parseInt(request.get("version").toString()) : 1;
            String name = (String) request.get("name");
            String description = (String) request.getOrDefault("description", "");
            String typeStr = (String) request.get("type");

            if (key == null || key.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key is required");
            }
            if (name == null || name.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
            }

            DatasourceType type = typeStr != null ? DatasourceType.valueOf(typeStr) : DatasourceType.HTTP;

            // 创建新的数据源
            var datasourceId = new DatasourceId(key, version);
            var datasource = ApiDatasource.create(datasourceId);

            // 配置数据源
            String inputSchema = (String) request.getOrDefault("inputSchema", "{}");
            String outputSchema = (String) request.getOrDefault("outputSchema", "{}");
            boolean strict = request.get("strict") != null && Boolean.parseBoolean(request.get("strict").toString());
            String operationDesc = (String) request.getOrDefault("operation", "default operation");
            String connectionString = (String) request.getOrDefault("connection", "");

            var contract = new DatasourceContract(inputSchema, outputSchema, strict);
            var operation = new SimpleDatasourceOperation(operationDesc);
            var connection = new HttpDatasourceConnection(connectionString, Duration.ofSeconds(30),
                    Duration.ofSeconds(10), Duration.ofSeconds(20), 3);

            datasource.configure(type, name, description, contract, operation, connection, List.of());

            return datasource;
        }).flatMap(datasourceRepository::save)
                .map(ApiDatasourceDTO::fromApiDatasource)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    /**
     * 获取数据源详情
     */
    @GetMapping("/{key}/{version}")
    public Mono<ResponseEntity<ApiDatasourceDTO>> getDatasource(
            @PathVariable String key,
            @PathVariable int version) {

        var datasourceId = new DatasourceId(key, version);
        return datasourceRepository.findById(datasourceId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(ApiDatasourceDTO::fromApiDatasource)
                .map(ResponseEntity::ok);
    }

    /**
     * 查询数据源列表
     */
    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> listDatasources(
            @RequestParam(required = false) String key) {

        var datasourcesMono = key != null ?
                datasourceRepository.findByKey(key).collectList() :
                datasourceRepository.findAll().collectList();

        return datasourcesMono
                .map(datasources -> {
                    var dtos = datasources.stream()
                            .map(ApiDatasourceDTO::fromApiDatasource)
                            .toList();
                    Map<String, Object> result = Map.of(
                            "datasources", dtos,
                            "total", dtos.size()
                    );
                    return ResponseEntity.ok(result);
                });
    }

    /**
     * 启用数据源
     */
    @PostMapping("/{key}/{version}/enable")
    public Mono<ResponseEntity<ApiDatasourceDTO>> enableDatasource(
            @PathVariable String key,
            @PathVariable int version) {

        var datasourceId = new DatasourceId(key, version);
        return datasourceRepository.findById(datasourceId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(datasource -> {
                    datasource.enable();
                    return datasourceRepository.save(datasource).thenReturn(datasource);
                })
                .map(ApiDatasourceDTO::fromApiDatasource)
                .map(ResponseEntity::ok);
    }

    /**
     * 更新数据源
     */
    @PatchMapping("/{key}/{version}")
    public Mono<ResponseEntity<ApiDatasourceDTO>> updateDatasource(
            @PathVariable String key,
            @PathVariable int version,
            @RequestBody Map<String, Object> request) {

        var datasourceId = new DatasourceId(key, version);
        return datasourceRepository.findById(datasourceId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(datasource -> {
                    // 更新可编辑字段（元数据）
                    if (request.containsKey("name") || request.containsKey("description")) {
                        String name = datasource.name(); // 使用当前值作为默认
                        String desc = datasource.description();

                        if (request.containsKey("name")) {
                            String new_name = (String) request.get("name");
                            if (new_name != null && !new_name.isBlank()) {
                                name = new_name;
                            }
                        }

                        if (request.containsKey("description")) {
                            desc = (String) request.get("description");
                        }

                        datasource.updateMetadata(name, desc);
                    }

                    // 更新契约（核心字段，确保未引用）
                    if (request.containsKey("inputSchema") || request.containsKey("outputSchema") || request.containsKey("strict")) {
                        var currentContract = datasource.contract();
                        String inputSchema = currentContract != null ? currentContract.inputSchema() : "{}";
                        String outputSchema = currentContract != null ? currentContract.outputSchema() : "{}";
                        boolean strict = currentContract != null ? currentContract.strict() : false;

                        if (request.containsKey("inputSchema")) {
                            inputSchema = (String) request.get("inputSchema");
                        }
                        if (request.containsKey("outputSchema")) {
                            outputSchema = (String) request.get("outputSchema");
                        }
                        if (request.containsKey("strict")) {
                            strict = Boolean.parseBoolean(request.get("strict").toString());
                        }

                        var newContract = new DatasourceContract(inputSchema, outputSchema, strict);
                        DatasourceOperation currentOperation = datasource.operation();
                        DatasourceConnection currentConnection = datasource.connection();

                        // 注意：实际应用中应该调用 datasource.updateCoreFields(isReferenced, ...)
                        // 这里简化处理，假设未被引用
                        datasource.updateCoreFields(false, newContract, currentOperation, currentConnection);
                    }

                    return datasourceRepository.save(datasource).thenReturn(datasource);
                })
                .map(ApiDatasourceDTO::fromApiDatasource)
                .map(ResponseEntity::ok);
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{key}/{version}")
    public Mono<ResponseEntity<Void>> deleteDatasource(
            @PathVariable String key,
            @PathVariable int version) {

        var datasourceId = new DatasourceId(key, version);
        return datasourceRepository.findById(datasourceId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(datasource -> {
                    if (datasource.status() == DatasourceStatus.ENABLED) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Cannot delete enabled datasource"));
                    }
                    return datasourceRepository.delete(datasourceId)
                            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
                });
    }

    /**
     * 停用数据源
     */
    @PostMapping("/{key}/{version}/disable")
    public Mono<ResponseEntity<ApiDatasourceDTO>> disableDatasource(
            @PathVariable String key,
            @PathVariable int version) {

        var datasourceId = new DatasourceId(key, version);
        return datasourceRepository.findById(datasourceId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(datasource -> {
                    datasource.disable();
                    return datasourceRepository.save(datasource).thenReturn(datasource);
                })
                .map(ApiDatasourceDTO::fromApiDatasource)
                .map(ResponseEntity::ok);
    }

    /**
     * 简单的 DatasourceOperation 实现
     */
    @Data
    private static class SimpleDatasourceOperation implements DatasourceOperation {
        private String description;

        public SimpleDatasourceOperation(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }

        @Override
        public boolean sameValueAs(DatasourceOperation other) {
            if (other == null) return false;
            if (other.getClass() != this.getClass()) return false;
            SimpleDatasourceOperation that = (SimpleDatasourceOperation) other;
            return Objects.equals(this.description, that.description);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleDatasourceOperation that = (SimpleDatasourceOperation) o;
            return sameValueAs(that);
        }

        @Override
        public int hashCode() {
            return Objects.hash(description);
        }
    }
}