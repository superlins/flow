package com.zwtech.flow.connector.filter;

import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;

/**
 * GlobalFilter 的包装器，将其适配为 ConnectorFilter 接口
 * 用于将 Spring Bean 的 GlobalFilter 参与到 PF4J 插件过滤器链中
 *
 * @param <REQ>  RequestSpec 类型
 * @param <RESP> ResponseSpec 类型
 * @author renc
 */
public class GlobalFilterWrapper<REQ extends RequestSpec, RESP extends ResponseSpec>
        implements ConnectorFilter<REQ, RESP>, Ordered {

    private final GlobalFilter<REQ, RESP> delegate;
    private final int order;

    public GlobalFilterWrapper(GlobalFilter<REQ, RESP> delegate, int order) {
        this.delegate = delegate;
        this.order = order;
    }

    @Override
    public Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope,
            ConnectorFilterChain<REQ, RESP> chain) {
        return delegate.filter(envelope, chain);
    }

    @Override
    public int getOrder() {
        return order;
    }

    public GlobalFilter<REQ, RESP> getDelegate() {
        return delegate;
    }
}
