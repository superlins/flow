package com.zwtech.flow.connector.factory;

/**
 * @author renc
 */
public abstract class ConnectorConfig {

    public static final ConnectorConfig EMPTY = new ConnectorConfig() {};

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
