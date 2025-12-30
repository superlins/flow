package com.zwtech.flow.core;

import java.util.Optional;

/**
 * @author renc
 */
public interface ExecutionEnvelope<REQ, RESP> {

    REQ requestSpec();

    ExecutionEnvelope<REQ, RESP> withRequestSpec(REQ request);

    Optional<RESP> responseSpec();

    ExecutionEnvelope<REQ, RESP> withResponseSpec(RESP response);

    ExecutionAttributes attributes();
}
