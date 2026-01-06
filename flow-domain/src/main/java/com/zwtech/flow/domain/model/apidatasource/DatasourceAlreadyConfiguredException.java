package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.DomainException;

/**
 * Datasource 已配置异常
 *
 * @author renc
 */
public class DatasourceAlreadyConfiguredException extends DomainException {
    
    public DatasourceAlreadyConfiguredException(DatasourceId id) {
        super("Datasource " + id + " is already configured");
    }
}

