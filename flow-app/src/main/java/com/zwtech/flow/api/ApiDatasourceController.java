package com.zwtech.flow.api;

import com.zwtech.flow.api.dto.ApiDatasourceDTO;
import com.zwtech.flow.domain.model.apidatasource.*;
import com.zwtech.flow.domain.model.apidatasource.connection.HttpDatasourceConnection;
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
                .map(datasource -> {
                    datasource.enable();
                    return datasource;
                })
                .flatMap(datasourceRepository::save)
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
                .map(datasource -> {
                    // 更新可编辑字段
                    if (request.containsKey("name")) {
                        String name = (String) request.get("name");
                        if (name != null && !name.isBlank()) {
                            datasource.name = name;
                        }
                    }
                    if (request.containsKey("description")) {
                        datasource.description = (String) request.get("description");
                    }
                    if (request.containsKey("inputSchema")) {
                        String inputSchema = (String) request.get("inputSchema");
                        if (inputSchema != null) {
                            datasource.contract = datasource.contract.withInputSchema(inputSchema);
                        }
                    }
                    if (request.containsKey("outputSchema")) {
                        String outputSchema = (String) request.get("outputSchema");
                        if (outputSchema != null) {
                            datasource.contract = datasource.contract.withOutputSchema(outputSchema);
                        }
                    }
                    if (request.containsKey("strict")) {
                        boolean strict = Boolean.parseBoolean(request.get("strict").toString());
                        datasource.contract = datasource.contract.withStrict(strict);
                    }
                    if (request.containsKey("connection")) {
                        String connectionString = (String) request.get("connection");
                        if (connectionString != null) {
                            var newConnection = new HttpDatasourceConnection(connectionString,
                                    Duration.ofSeconds(30), Duration.ofSeconds(10),
                                    Duration.ofSeconds(20), 3);
                            datasource.connection = newConnection;
                        }
                    }
                    return datasource;
                })
                .flatMap(datasourceRepository::save)
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
                    if (datasource.status == DatasourceStatus.ENABLED) {
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
                .map(datasource -> {
                    datasource.disable();
                    return datasource;
                })
                .flatMap(datasourceRepository::save)
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
