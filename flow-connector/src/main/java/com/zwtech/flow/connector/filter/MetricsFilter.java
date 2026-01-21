package com.zwtech.flow.connector.filter;

import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 指标过滤器，使用 Micrometer 采集连接器执行指标
 * <p>
 * 采集指标：
 * - 请求计数（按状态、类型、数据源）
 * - 响应时间分布
 * - 活跃请求数
 * - 错误率
 *
 * @author renc
 */
@Component
public class MetricsFilter<REQ extends RequestSpec, RESP extends ResponseSpec>
        implements GlobalFilter<REQ, RESP>, Ordered {

    private static final Logger log = LoggerFactory.getLogger(MetricsFilter.class);

    private static final String REQUEST_START_TIME_ATTR = "metricsRequestStartTime";

    private final MeterRegistry meterRegistry;

    public MetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope,
            ConnectorFilterChain<REQ, RESP> chain) {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        ExecutionEnvelope<REQ, RESP> envelopeWithAttrs = envelope.withAttributes(
                envelope.attributes().with(REQUEST_START_TIME_ATTR, startTime));

        // 获取 Connector 类型
        String connectorType = getConnectorType(envelope.requestSpec());

        // 活跃请求数 +1
        incrementActiveRequests(connectorType);

        return chain.filter(envelopeWithAttrs)
                .doOnSuccess(successEnvelope -> recordSuccess(successEnvelope, connectorType, startTime))
                .doOnError(error -> recordError(connectorType, startTime, error))
                .doFinally(signal -> decrementActiveRequests(connectorType))
                // 出错时仍返回，让链继续传播
                .onErrorResume(error -> Mono.just(envelopeWithAttrs));
    }

    /**
     * 记录成功指标
     */
    private void recordSuccess(ExecutionEnvelope<REQ, RESP> envelope, String connectorType, long startTime) {
        long duration = System.currentTimeMillis() - startTime;

        // 记录请求计数
        Counter.builder("connector.requests.total")
                .tag("connector", connectorType)
                .tag("status", "success")
                .register(meterRegistry)
                .increment();

        // 记录响应时间
        Timer.builder("connector.requests.duration")
                .tag("connector", connectorType)
                .description("Connector request duration")
                .register(meterRegistry)
                .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);

        if (log.isDebugEnabled()) {
            log.debug("Metrics: Request to {} succeeded in {}ms", connectorType, duration);
        }
    }

    /**
     * 记录错误指标
     */
    private void recordError(String connectorType, long startTime, Throwable error) {
        long duration = System.currentTimeMillis() - startTime;
        String errorType = error != null ? error.getClass().getSimpleName() : "unknown";

        // 记录错误计数
        Counter.builder("connector.requests.total")
                .tag("connector", connectorType)
                .tag("status", "error")
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();

        // 记录错误响应时间
        Timer.builder("connector.requests.duration")
                .tag("connector", connectorType)
                .tag("status", "error")
                .description("Connector error request duration")
                .register(meterRegistry)
                .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);

        log.error("Metrics: Request to {} failed after {}ms, Error: {}",
                connectorType, duration, errorType);
    }

    /**
     * 增加活跃请求数
     */
    private void incrementActiveRequests(String connectorType) {
        meterRegistry.gauge("connector.requests.active",
                io.micrometer.core.instrument.Tags.of("connector", connectorType),
                1,
                value -> value + 1);
    }

    /**
     * 减少活跃请求数
     */
    private void decrementActiveRequests(String connectorType) {
        meterRegistry.gauge("connector.requests.active",
                io.micrometer.core.instrument.Tags.of("connector", connectorType),
                1,
                value -> value - 1);
    }

    /**
     * 从 RequestSpec 获取 Connector 类型
     * 默认使用类名，子类可以覆盖提供更有意义的标识
     */
    private String getConnectorType(REQ request) {
        if (request == null) {
            return "unknown";
        }
        String className = request.getClass().getSimpleName();
        // 移除 "RequestSpec" 后缀
        if (className.endsWith("RequestSpec")) {
            return className.substring(0, className.length() - "RequestSpec".length());
        }
        return className;
    }

    @Override
    public int getOrder() {
        // 指标过滤器优先级较低，在日志过滤器之前
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
