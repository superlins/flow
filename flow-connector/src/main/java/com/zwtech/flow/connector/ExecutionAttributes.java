package com.zwtech.flow.connector;

import java.util.Optional;

/**
 * @author renc
 */
public interface ExecutionAttributes {

    <T> Optional<T> get(String key);

    ExecutionAttributes with(String key, Object value);
}
