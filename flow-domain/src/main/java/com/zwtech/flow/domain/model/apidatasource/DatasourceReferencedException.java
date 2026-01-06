package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.DomainException;

/**
 * Datasource 被引用异常
 * 
 * 实现 DS-1 规则：被引用的 Datasource 不可修改核心字段
 *
 * @author renc
 */
public class DatasourceReferencedException extends DomainException {
    
    public DatasourceReferencedException(DatasourceId id) {
        super("Cannot modify core fields of referenced datasource " + id + ". " +
              "Create a new version instead (DS-1 rule)");
    }
}

