package com.zwtech.flow.connector;

import java.util.Objects;
import java.util.Optional;

/**
 * ExecutionEnvelope 的不可变实现。
 *
 * @author renc
 */
public final class DefaultExecutionEnvelope<REQ extends RequestSpec, RESP extends ResponseSpec>
        implements ExecutionEnvelope<REQ, RESP> {

    private final REQ requestSpec;
    private final Optional<RESP> responseSpec;
    private final ExecutionAttributes attributes;

    public DefaultExecutionEnvelope(REQ requestSpec, Optional<RESP> responseSpec, ExecutionAttributes attributes) {
        this.requestSpec = Objects.requireNonNull(requestSpec, "requestSpec must not be null");
        this.responseSpec = responseSpec == null ? Optional.empty() : responseSpec;
        this.attributes = attributes == null ? DefaultExecutionAttributes.empty() : attributes;
    }

    public static <REQ extends RequestSpec, RESP extends ResponseSpec> DefaultExecutionEnvelope<REQ, RESP> of(REQ req) {
        return new DefaultExecutionEnvelope<>(req, Optional.empty(), DefaultExecutionAttributes.empty());
    }

    @Override
    public REQ requestSpec() {
        return requestSpec;
    }

    @Override
    public Optional<RESP> responseSpec() {
        return responseSpec;
    }

    @Override
    public ExecutionAttributes attributes() {
        return attributes;
    }

    @Override
    public ExecutionEnvelope<REQ, RESP> withRequestSpec(REQ newRequest) {
        return new DefaultExecutionEnvelope<>(newRequest, responseSpec, attributes);
    }

    @Override
    public ExecutionEnvelope<REQ, RESP> withResponseSpec(RESP newResponse) {
        return new DefaultExecutionEnvelope<>(requestSpec, Optional.ofNullable(newResponse), attributes);
    }

    @Override
    public ExecutionEnvelope<REQ, RESP> withAttributes(ExecutionAttributes newAttributes) {
        return new DefaultExecutionEnvelope<>(requestSpec, responseSpec, newAttributes);
    }
}

