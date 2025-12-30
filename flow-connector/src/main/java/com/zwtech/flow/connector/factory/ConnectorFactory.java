package com.zwtech.flow.connector.factory;

import com.zwtech.flow.connector.Connector;
import com.zwtech.flow.connector.RequestSpec;
import com.zwtech.flow.connector.ResponseSpec;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;

/**
 * @author renc
 */
public interface ConnectorFactory<REQ extends RequestSpec, RESP extends ResponseSpec> {

    Connector<REQ, RESP> create(ApiDatasource apiDatasource);
}
