// package com.zwtech.flow.core.server;
//
// import org.springframework.lang.Nullable;
//
// import java.util.Map;
// import java.util.function.Consumer;
//
// /**
//  * @author renc
//  */
// public interface ServerUpstreamResponse {
//
//     @Nullable
//     Object getRawData();
//
//     Map<String, Object> getBody();
//
//     Map<String, String> getHeaders();
//
//     default Builder mutate() {
//         return new DefaultServerUpstreamResponseBuilder(this);
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
//         ServerUpstreamResponse build();
//     }
// }
