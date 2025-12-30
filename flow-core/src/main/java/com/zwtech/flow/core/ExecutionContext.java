package com.zwtech.flow.core;

import tools.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * @author renc
 */
public interface ExecutionContext {

    Optional<JsonNode> input();

    Optional<JsonNode> output();

    Optional<DerivedContext> derived();
}
