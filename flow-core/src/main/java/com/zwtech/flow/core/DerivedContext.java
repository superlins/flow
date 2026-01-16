package com.zwtech.flow.core;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author renc
 */
public interface DerivedContext {

    JsonNode get(String path);

    boolean contains(String path);

    DerivedContext with(String path, JsonNode value);
}
