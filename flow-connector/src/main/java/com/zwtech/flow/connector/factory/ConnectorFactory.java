package com.zwtech.flow.connector.factory;

import org.example.core.connector.Connector;
import org.example.core.connector.RequestSpec;
import org.example.core.connector.ResponseSpec;

/**
 * @author renc
 */
public interface ConnectorFactory<C extends ConnectorConfig, REQ extends RequestSpec, RESP extends ResponseSpec> extends Nameable {

    Connector<REQ, RESP> newInstance(C config);
}
