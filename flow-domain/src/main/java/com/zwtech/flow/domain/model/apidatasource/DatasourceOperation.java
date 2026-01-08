package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.model.apidatasource.behavior.OperationBehavior;
import com.zwtech.flow.domain.shared.ValueObject;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Datasource Operation
 * 表示一个数据源操作，包含契约、行为和扩展
 * 
 * @author renc
 */
public class DatasourceOperation implements ValueObject<DatasourceOperation> {

    private String key;
    private OperationContract contract;
    private OperationBehavior behavior;
    private List<OperationExtension> extensions;

    public DatasourceOperation() {
        this.extensions = new ArrayList<>();
    }

    public DatasourceOperation(String key, OperationContract contract, OperationBehavior behavior, List<OperationExtension> extensions) {
        Assert.hasText(key, "key must not be empty");
        Assert.notNull(contract, "contract must not be null");
        Assert.notNull(behavior, "behavior must not be null");
        this.key = key;
        this.contract = contract;
        this.behavior = behavior;
        this.extensions = extensions != null ? new ArrayList<>(extensions) : new ArrayList<>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public OperationContract getContract() {
        return contract;
    }

    public void setContract(OperationContract contract) {
        this.contract = contract;
    }

    public OperationBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(OperationBehavior behavior) {
        this.behavior = behavior;
    }

    public List<OperationExtension> getExtensions() {
        return extensions != null ? new ArrayList<>(extensions) : new ArrayList<>();
    }

    public void setExtensions(List<OperationExtension> extensions) {
        this.extensions = extensions != null ? new ArrayList<>(extensions) : new ArrayList<>();
    }

    @Override
    public boolean sameValueAs(DatasourceOperation other) {
        if (other == null) {
            return false;
        }
        return Objects.equals(this.key, other.key)
                && Objects.equals(this.contract, other.contract)
                && Objects.equals(this.behavior, other.behavior)
                && Objects.equals(this.extensions, other.extensions);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DatasourceOperation other) && sameValueAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, contract, behavior, extensions);
    }
}
