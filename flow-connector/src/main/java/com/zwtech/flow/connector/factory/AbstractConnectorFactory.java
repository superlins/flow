package com.zwtech.flow.connector.factory;

import org.example.core.connector.RequestSpec;
import org.example.core.connector.ResponseSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author renc
 */
public abstract class AbstractConnectorFactory<C extends ConnectorConfig, REQ extends RequestSpec, RESP extends ResponseSpec>
        implements ConnectorFactory<C, REQ, RESP> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnectorFactory.class);

}
