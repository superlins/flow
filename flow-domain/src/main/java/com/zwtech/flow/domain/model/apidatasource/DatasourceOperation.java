package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.model.apidatasource.behavior.OperationBehavior;
import com.zwtech.flow.domain.shared.ValueObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author renc
 */
public class DatasourceOperation implements ValueObject<DatasourceOperation> {

    private String key;

    private OperationContract contract;

    private OperationBehavior behavior;

    private List<OperationExtension> extensions;

    public DatasourceOperation() {
        this.extensions = new ArrayList<>();

        // if (this.contract != null) {
        //     throw new DatasourceAlreadyConfiguredException(id);
        // }
    }

    @Override
    public boolean sameValueAs(DatasourceOperation other) {
        return false;
    }
}
