package com.zwtech.flow.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author renc
 */
public abstract class AbstractConnector<REQ extends RequestSpec, RESP extends ResponseSpec> implements Connector<REQ, RESP> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnector.class);
}
