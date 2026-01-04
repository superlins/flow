package com.zwtech.flow.domain.service;

/**
 * @author renc
 */
public interface SchemaValidationService {

    void validate(String schema, Object input);
}
