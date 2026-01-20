package com.zwtech.flow.connector.specs;

import com.zwtech.flow.connector.factory.ConnectorEndpointTypeNames;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import org.springframework.stereotype.Component;

/**
 * HTTP 数据源规格工厂
 *
 * @author renc
 */
@Component
public class HttpDatasourceSpecsFactory implements DatasourceSpecsFactory {

    @Override
    public boolean supports(String type) {
        return ConnectorEndpointTypeNames.HTTP.equalsIgnoreCase(type);
    }

    @Override
    public DatasourceSpecs create(ApiDatasource datasource) {
        return HttpDatasourceSpecs.from(datasource);
    }
}
