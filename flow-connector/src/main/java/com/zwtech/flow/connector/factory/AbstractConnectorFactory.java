package com.zwtech.flow.connector.factory;

import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author renc
 */
public abstract class AbstractConnectorFactory<REQ extends RequestSpec, RESP extends ResponseSpec>
        implements ConnectorFactory<REQ, RESP> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnectorFactory.class);

}
