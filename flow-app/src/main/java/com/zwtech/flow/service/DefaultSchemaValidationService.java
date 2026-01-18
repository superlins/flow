package com.zwtech.flow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.InputFormat;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import com.zwtech.flow.domain.service.SchemaValidationService;
import com.zwtech.flow.domain.shared.DomainException;
import org.springframework.stereotype.Service;

/**
 * Default implementation of SchemaValidationService using NetworkNT JSON Schema Validator
 *
 * @author renc
 */
@Service
public class DefaultSchemaValidationService implements SchemaValidationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchemaRegistry schemaRegistry;

    public DefaultSchemaValidationService() {
        // Use Draft 2020-12 as the default dialect
        this.schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
    }

    @Override
    public void validate(String schema, Object input) throws DomainException {
        try {
            var jsonSchema = schemaRegistry.getSchema(schema);

            // Convert input to string for validation
            String inputJson;
            if (input instanceof String) {
                inputJson = (String) input;
            } else {
                inputJson = objectMapper.writeValueAsString(input);
            }

            var errors = jsonSchema.validate(inputJson, InputFormat.JSON);

            if (!errors.isEmpty()) {
                var errorMsg = new StringBuilder("Schema validation failed: ");
                errors.forEach(error -> errorMsg.append(error.toString()).append("; "));
                throw new DomainException(errorMsg.toString());
            }
        } catch (DomainException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainException("Schema validation error: " + e.getMessage(), e);
        }
    }
}
