package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.DomainException;

/**
 * Datasource 未配置异常
 *
 * @author renc
 */
public class DatasourceNotConfiguredException extends DomainException {
    
    public DatasourceNotConfiguredException(DatasourceId id) {
        super("Datasource " + id + " is not configured");
    }
}

