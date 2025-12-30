// package com.zwtech.flow.core.server;
//
// import org.springframework.web.reactive.function.server.ServerRequest;
//
// import java.util.List;
// import java.util.Map;
// import java.util.function.Consumer;
//
// /**
//  * @author renc
//  */
// public interface ServerUpstreamRequest {
//
//     ServerRequest getOriginalServerRequest();
//     Object getContextObject();
//     Map<String, Object> getBody();
//     Map<String, String> getHeaders();
//     Map<String, String> getQueryParams();
//     Map<String, Object> getPathVariables();
//     List<Object> getSqlParameters();
//     Map<String, Object> getNamedSqlParameters();
//     List<Object> getRpcMethodArgs();
//     Map<String, Object> getGeneralParameters();
//
//     default Builder mutate() {
//         return new DefaultServerUpstreamRequestBuilder(this);
//     }
//
//     interface Builder {
//
//         Builder body(Consumer<Map<String, Object>> bodyConsumer);
//         Builder body(String key, Object value);
//
//         Builder headers(Consumer<Map<String, String>> headersConsumer);
//         Builder header(String key, String value);
//
//         Builder queryParams(Consumer<Map<String, String>> queryParamsConsumer);
//         Builder queryParam(String key, String value);
//
//         Builder pathVariables(Consumer<Map<String, Object>> pathVariablesConsumer);
//         Builder pathVariable(String key, Object value);
//
//         Builder sqlParameters(Consumer<List<Object>> sqlParametersConsumer);
//         Builder sqlParameter(Object value);
//
//         Builder namedSqlParameters(Consumer<Map<String, Object>> namedSqlParametersConsumer);
//         Builder namedSqlParameter(String key, Object value);
//
//         Builder rpcMethodArgs(Consumer<List<Object>> rpcMethodArgsConsumer);
//         Builder rpcMethodArg(Object value);
//
//         Builder generalParameters(Consumer<Map<String, Object>> generalParametersConsumer);
//         Builder generalParameter(String key, Object value);
//
//         ServerUpstreamRequest build();
//     }
// }
