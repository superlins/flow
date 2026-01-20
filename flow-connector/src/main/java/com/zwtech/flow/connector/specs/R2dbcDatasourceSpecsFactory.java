package com.zwtech.flow.connector.specs;

import com.zwtech.flow.connector.factory.ConnectorEndpointTypeNames;
import com.zwtech.flow.domain.model.apidatasource.ApiDatasource;
import org.springframework.stereotype.Component;

/**
 * R2DBC 数据源规格工厂
 *
 * @author renc
 */
@Component
public class R2dbcDatasourceSpecsFactory implements DatasourceSpecsFactory {

    @Override
    public boolean supports(String type) {
        return ConnectorEndpointTypeNames.R2DBC.equalsIgnoreCase(type);
    }

    @Override
    public DatasourceSpecs create(ApiDatasource datasource) {
        return R2dbcDatasourceSpecs.from(datasource);
    }
}
