package com.zwtech.flow.domain.model.apidatasource;

import com.zwtech.flow.domain.shared.Contract;

import java.util.Objects;

/**
 * Datasource 契约
 * <p>
 * 继承自 {@link Contract}，添加 strict 模式支持。
 * strict=true 时，输入输出必须严格匹配 Schema。
 *
 * @author renc
 */
public final class DatasourceContract extends Contract {

    private final boolean strict;

    public DatasourceContract(String inputSchema, String outputSchema, boolean strict) {
        super(inputSchema, outputSchema);
        this.strict = strict;
    }

    /**
     * 创建非严格模式的契约
     */
    public static DatasourceContract of(String inputSchema, String outputSchema) {
        return new DatasourceContract(inputSchema, outputSchema, false);
    }

    /**
     * 是否为严格模式
     */
    public boolean strict() {
        return strict;
    }

    @Override
    public boolean sameValueAs(Contract other) {
        if (!super.sameValueAs(other)) {
            return false;
        }
        if (!(other instanceof DatasourceContract that)) {
            return false;
        }
        return strict == that.strict;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DatasourceContract that = (DatasourceContract) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), strict);
    }

    @Override
    public String toString() {
        return "DatasourceContract{strict=" + strict + "}";
    }

    /* Helper methods for updating individual fields */
    public DatasourceContract withInputSchema(String inputSchema) {
        return new DatasourceContract(inputSchema, outputSchema(), this.strict);
    }

    public DatasourceContract withOutputSchema(String outputSchema) {
        return new DatasourceContract(inputSchema(), outputSchema, this.strict);
    }

    public DatasourceContract withStrict(boolean strict) {
        return new DatasourceContract(inputSchema(), outputSchema(), strict);
    }
}