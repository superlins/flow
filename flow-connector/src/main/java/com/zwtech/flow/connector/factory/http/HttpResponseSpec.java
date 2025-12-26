package com.zwtech.flow.connector.factory.http;

import org.example.core.connector.ResponseSpec;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * @author renc
 */
public class HttpResponseSpec implements ResponseSpec {

    private HttpStatusCode statusCode;
    private HttpHeaders headers;
    private byte[] body;

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public HttpResponseSpec setStatusCode(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpResponseSpec setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public HttpResponseSpec setBody(byte[] body) {
        this.body = body;
        return this;
    }

}
