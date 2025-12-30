package com.zwtech.flow.domain.model.apiservice;

import org.springframework.util.Assert;

/**
 * @author renc
 */
public final class DatasourceRef {

    private final String datasourceId;
    private final String version;

    public DatasourceRef(String datasourceId, String version) {
        Assert.hasText(datasourceId, "datasourceId must not be blank");
        Assert.hasText(version, "version must not be blank");
        this.datasourceId = datasourceId;
        this.version = version;
    }

    public String datasourceId() {
        return datasourceId;
    }

    public String version() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        DatasourceRef that = (DatasourceRef) o;
        return datasourceId.equals(that.datasourceId) && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        int result = datasourceId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
