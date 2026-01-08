package com.zwtech.flow.core;

/**
 * @author renc
 */
public interface VariableContext {

    //阶段,命名空间 (Namespace),说明,读写权限
    //1. 初始态,$input / $args,来自 ApiService 传入的参数 (符合 Contract.Input),只读
    //,$env,"环境变量 (如 K8s namespace, region)",只读
    //,$secrets,敏感配置 (从 Vault/K8s Secret 加载),只读
    //2. 转换态,$req,正在构建的发送给上游的 HTTP 请求对象,可写
    //3. 执行态,$res / $upstream,"上游返回的原始 HTTP 响应 (status, headers, body)",只读
    //4. 终态,$output,最终计算出的符合 Contract.Output 的结果,可写
}
