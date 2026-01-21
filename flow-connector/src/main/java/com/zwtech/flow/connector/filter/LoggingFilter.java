package com.zwtech.flow.connector.filter;

import com.zwtech.flow.connector.ExecutionEnvelope;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 日志过滤器，参考 Spring Cloud Gateway 的日志过滤器设计
 * <p>
 * 记录请求和响应的摘要信息
 *
 * @author renc
 */
@Component
public class LoggingFilter<REQ extends RequestSpec, RESP extends ResponseSpec>
        implements GlobalFilter<REQ, RESP>, Ordered {

    public static final String REQUEST_START_TIME_ATTR = "requestStartTime";

    private final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope,
            ConnectorFilterChain<REQ, RESP> chain) {
        // Pre 阶段：记录请求开始时间，输出请求日志
        long startTime = System.currentTimeMillis();
        ExecutionEnvelope<REQ, RESP> envelopeWithAttrs = envelope.withAttributes(
                envelope.attributes().with(REQUEST_START_TIME_ATTR, startTime));

        logRequest(envelope);

        // 继续处理链
        return chain.filter(envelopeWithAttrs)
                .doOnSuccess(successEnvelope -> logResponse(successEnvelope, startTime, null))
                .doOnError(error -> logResponse(envelopeWithAttrs, startTime, error))
                // 出错时仍返回原始 envelope，让链继续传播
                .onErrorResume(error -> Mono.just(envelopeWithAttrs));
    }

    /**
     * 记录请求日志（Pre 阶段）
     */
    private void logRequest(ExecutionEnvelope<REQ, RESP> envelope) {
        if (log.isInfoEnabled()) {
            REQ request = envelope.requestSpec();
            String requestInfo = request.toString();
            log.info("Connector Request: {}", requestInfo);
        }
    }

    /**
     * 记录响应日志（Post 阶段）
     */
    private void logResponse(ExecutionEnvelope<REQ, RESP> envelope, long startTime, Throwable error) {
        long duration = System.currentTimeMillis() - startTime;

        if (error != null) {
            log.error("Connector Error: Request failed after {}ms, Error: {}",
                    duration, error.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error details:", error);
            }
        } else {
            Optional<RESP> responseOpt = envelope.responseSpec();
            if (responseOpt.isPresent()) {
                RESP response = responseOpt.get();
                log.info("Connector Response: Completed in {}ms, Response: {}",
                        duration, response.toString());
            } else {
                log.warn("Connector Response: No response available after {}ms", duration);
            }
        }
    }

    @Override
    public int getOrder() {
        // 日志过滤器优先级较低，确保在其他过滤器之后执行
        return Ordered.LOWEST_PRECEDENCE;
    }
}
