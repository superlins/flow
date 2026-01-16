package com.zwtech.flow.core;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * @author renc
 */
public interface ExecutionContext {

    Optional<JsonNode> input();

    Optional<JsonNode> output();

    Optional<DerivedContext> derived();
}
