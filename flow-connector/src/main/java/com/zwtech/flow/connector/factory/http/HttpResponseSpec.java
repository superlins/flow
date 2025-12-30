package com.zwtech.flow.connector.factory.http;

import com.zwtech.flow.connector.ResponseSpec;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import tools.jackson.databind.JsonNode;

/**
 * @author renc
 */
@Builder
@Getter
public class HttpResponseSpec implements ResponseSpec {

    private HttpStatusCode statusCode;
    private HttpHeaders headers;
    private JsonNode body;
}
