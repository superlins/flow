package com.zwtech.flow.connector;

import java.util.Optional;

/**
 * @author renc
 */
public interface ExecutionEnvelope<REQ extends RequestSpec, RESP extends ResponseSpec> {

    REQ requestSpec();

    Optional<RESP> responseSpec();

    ExecutionAttributes attributes();

    ExecutionEnvelope<REQ, RESP> withRequestSpec(REQ requestSpec);

    ExecutionEnvelope<REQ, RESP> withResponseSpec(RESP responseSpec);

    ExecutionEnvelope<REQ, RESP> withAttributes(ExecutionAttributes attributes);
}
