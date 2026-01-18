package com.zwtech.flow.connector.filter;

import org.springframework.core.Ordered;

/**
 * 通用的排序适配器接口，用于统一 GlobalFilter 和 ConnectorFilter 的排序
 *
 * @author renc
 */
public interface OrderedAdapter extends Ordered {

    /**
     * 获取过滤器的类型标识
     * 用于在构建过滤器链时进行类型匹配
     */
    String filterType();

    /**
     * 获取排序优先级
     * 值越小优先级越高
     */
    @Override
    int getOrder();
}
