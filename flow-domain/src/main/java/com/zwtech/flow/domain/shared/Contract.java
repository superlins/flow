package com.zwtech.flow.domain.shared;

import org.springframework.util.Assert;

import java.util.Objects;

/**
 * 通用契约值对象
 * 定义 JSON Schema 格式的输入输出契约
 * <p>
 * 作为 DatasourceContract、ServiceContract、WorkflowContract 的基类，
 * 提供统一的契约结构和校验逻辑。
 *
 * @author renc
 */
public class Contract implements ValueObject<Contract> {

    private final String inputSchema;
    private final String outputSchema;

    /**
     * 创建契约
     *
     * @param inputSchema  输入 JSON Schema（不能为空）
     * @param outputSchema 输出 JSON Schema（不能为空）
     */
    public Contract(String inputSchema, String outputSchema) {
        Assert.hasText(inputSchema, "inputSchema must not be empty");
        Assert.hasText(outputSchema, "outputSchema must not be empty");
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
    }

    /**
     * 创建空契约
     */
    public static Contract empty() {
        return new Contract("{}", "{}");
    }

    /**
     * 获取输入 JSON Schema
     */
    public String inputSchema() {
        return inputSchema;
    }

    /**
     * 获取输出 JSON Schema
     */
    public String outputSchema() {
        return outputSchema;
    }

    @Override
    public boolean sameValueAs(Contract other) {
        return other != null
                && inputSchema.equals(other.inputSchema)
                && outputSchema.equals(other.outputSchema);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Contract contract = (Contract) o;
        return sameValueAs(contract);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputSchema, outputSchema);
    }

    @Override
    public String toString() {
        return "Contract{inputSchema='" + inputSchema + "', outputSchema='" + outputSchema + "'}";
    }
}
