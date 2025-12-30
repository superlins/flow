// package com.zwtech.flow.core.server;
//
// import org.springframework.lang.Nullable;
// import org.springframework.util.Assert;
//
// import java.util.HashMap;
// import java.util.Map;
// import java.util.function.Consumer;
//
// /**
//  * @author renc
//  */
// class DefaultServerUpstreamResponseBuilder implements ServerUpstreamResponse.Builder {
//
//     @Nullable
//     private Object rawData;
//
//     private final Map<String, Object> body = new HashMap<>();
//     private final Map<String, String> headers = new HashMap<>();
//
//     private final Map<String, Object> finalClientResponseData = new HashMap<>();
//
//     public DefaultServerUpstreamResponseBuilder(ServerUpstreamResponse original) {
//         Assert.notNull(original, "ServerUpstreamResponse must not be null.");
//         this.rawData = original.getRawData();
//         this.body.putAll(original.getBody());
//         this.headers.putAll(original.getHeaders());
//     }
//
//     @Override
//     public ServerUpstreamResponse.Builder body(Consumer<Map<String, Object>> bodyConsumer) {
//         Assert.notNull(bodyConsumer, "bodyConsumer must not be null");
//         bodyConsumer.accept(this.body);
//         return this;
//     }
//
//     @Override
//     public ServerUpstreamResponse.Builder body(String key, Object value) {
//         this.body.put(key, value);
//         return this;
//     }
//
//     @Override
//     public ServerUpstreamResponse.Builder headers(Consumer<Map<String, String>> headersConsumer) {
//         Assert.notNull(headersConsumer, "headersConsumer must not be null");
//         headersConsumer.accept(this.headers);
//         return this;
//     }
//
//     @Override
//     public ServerUpstreamResponse.Builder header(String key, String value) {
//         this.headers.put(key, value);
//         return this;
//     }
//
//     @Override
//     public ServerUpstreamResponse build() {
//         return new MutatedServerUpstreamResponse(rawData, this.body, this.headers);
//     }
//
//     private static class MutatedServerUpstreamResponse implements ServerUpstreamResponse {
//
//         @Nullable
//         private final Object rawData;
//
//         private final Map<String, Object> body;
//
//         private final Map<String, String> headers;
//
//         public MutatedServerUpstreamResponse(@Nullable Object rawData, Map<String, Object> body, Map<String, String> headers) {
//             this.rawData = rawData;
//             this.body = body;
//             this.headers = headers;
//         }
//
//         @Override
//         public Object getRawData() {
//             return rawData;
//         }
//
//         @Override
//         public Map<String, Object> getBody() {
//             return this.body;
//         }
//
//         @Override
//         public Map<String, String> getHeaders() {
//             return this.headers;
//         }
//     }
// }
