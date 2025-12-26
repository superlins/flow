package com.zwtech.flow.connector.factory.cassandra;// package org.example.core.connector.factory.cassandra;
//
// import com.datastax.oss.driver.api.core.CqlSession;
// import com.datastax.oss.driver.api.core.cql.SimpleStatement;
// import org.example.core.connector.Connector;
// import org.example.core.connector.factory.AbstractConnectorFactory;
// import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
// import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
// import org.springframework.data.cassandra.core.cql.session.DefaultBridgedReactiveSession;
// import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
// import reactor.core.publisher.Mono;
//
// import java.net.InetSocketAddress;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
//
// /**
//  * @author renc
//  */
// public class CassandraConnectorFactory extends AbstractConnectorFactory<CassandraRequestSpec, CassandraResponseSpec> {
//
//     private final Map<Object, ReactiveCassandraTemplate> templateCache = new ConcurrentHashMap<>();
//
//     private final Map<Object, CqlSession> sessionCache = new ConcurrentHashMap<>();
//
//     @Override
//     public Connector<CassandraRequestSpec, CassandraResponseSpec> apply(Object config) {
//
//         ReactiveCassandraTemplate reactiveCassandraTemplate = templateCache.computeIfAbsent(config, this::createReactiveCassandraTemplate);
//
//         return spec -> {
//
//             spec.validate(); // 验证请求
//
//             return reactiveCassandraTemplate.getReactiveCqlOperations()
//                     .queryForMap(SimpleStatement.newInstance(spec.getCqlQuery(), spec.getBindValues()))
//                     .map(CassandraResponseSpec::new) // 将 ResultSet 包装到 CassandraResponseSpec
//                     .doOnSuccess(response -> LOGGER.info("Successfully executed Cassandra query: {}", spec.getCqlQuery()))
//                     .onErrorResume(Throwable.class, ex -> {
//                         LOGGER.error("[Cassandra Connector Error] Query: {}, Message: {}", spec.getCqlQuery(), ex.getMessage(), ex);
//                         // 返回一个包含错误信息的 CassandraResponseSpec
//                         // 根据您的 ResponseSpec 定义，可能需要一个包含错误信息的构造函数或设置方法
//
//                         return Mono.error(ex);
//                     }).flatMap(responseSpec -> {
//                         // set response to context
//                         return Mono.empty();
//                     });
//         };
//     }
//
//     private ReactiveCassandraTemplate createReactiveCassandraTemplate(CassandraEndpoint endpoint) {
//         LOGGER.info("Creating new CqlSession and ReactiveCassandraTemplate for config: {}", endpoint.getKeyspace());
//
//         // 1. 创建 CqlSession
//         CqlSession cqlSession = CqlSession.builder()
//                 .addContactPoint(new InetSocketAddress(endpoint.getContactPoints(), endpoint.getPort()))
//                 .withKeyspace(endpoint.getKeyspace())
//                 .withLocalDatacenter(endpoint.getLocalDatacenter())
//                 // .withAuthCredentials("username", "password") // 根据需要添加认证
//                 // .withSslContext(...) // 根据需要添加 SSL
//                 .build();
//
//         sessionCache.put(endpoint, cqlSession); // 缓存 CqlSession 以便后续关闭
//
//         // 2. 创建 MappingCassandraConverter (Spring Data Cassandra 需要)
//         // 即使没有实体映射，也需要一个 converter
//         CassandraMappingContext mappingContext = new CassandraMappingContext();
//         MappingCassandraConverter converter = new MappingCassandraConverter(mappingContext);
//
//         // 3. 创建 ReactiveCassandraTemplate
//         // 传入一个 ReactiveSession，它包装了 CqlSession
//         // Spring Data Cassandra 通常通过 ReactiveSession 实现 Reactive 接口
//         return new ReactiveCassandraTemplate(new DefaultBridgedReactiveSession(cqlSession), converter);
//     }
//
// }
