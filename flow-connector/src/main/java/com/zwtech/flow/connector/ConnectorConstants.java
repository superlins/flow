package com.zwtech.flow.connector;

/**
 * @author renc
 */
public final class ConnectorConstants {

    public static final String SERVICE_REGISTRY_ATTR = "";

    public static final String RAW_DATA_SOURCE_RESPONSE_ATTR = "";

    // CONTEXT NODE ATTRS FOR EXPR: `$.request.headers.xx` | `$.request.body.xx` | `$.request.queryParams.xx`
    public static final String MAPPED_CONTEXT_ATTR = "";
    public static final String MAPPED_CONTEXT_NODE_REQUEST_ATTR = "request";
    public static final String MAPPED_CONTEXT_NODE_HEADERS_ATTR = "headers";
    public static final String MAPPED_CONTEXT_NODE_QUERY_PARAMS_ATTR = "queryParams";
    public static final String MAPPED_CONTEXT_NODE_BODY_ATTR = "body";
}
