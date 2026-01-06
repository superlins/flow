package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.DomainException;

/**
 * Datasource 未启用异常
 * 
 * 实现 DS-2 规则：只有 Enabled 状态才允许调用
 *
 * @author renc
 */
public class DatasourceNotEnabledException extends DomainException {
    
    public DatasourceNotEnabledException(DatasourceId id) {
        super("Datasource " + id + " is not enabled (DS-2 rule)");
    }
}

