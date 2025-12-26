package com.zwtech.flow.connector.factory;

import org.example.core.connector.RequestSpec;
import org.example.core.connector.ResponseSpec;
import org.springframework.core.ResolvableType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectorFactoryTypeUtils {

    protected static final Map<Class<?>, ResolvableType> RESOLVED_TYPE_CACHE = new ConcurrentHashMap<>();

    private static ResolvableType resolveType(Class<?> factoryClass) {
        return RESOLVED_TYPE_CACHE.computeIfAbsent(factoryClass, clazz -> ResolvableType.forClass(ConnectorFactory.class, clazz));
    }

    @SuppressWarnings("unchecked")
    public static <T extends ConnectorConfig> Class<T> getEndpointType(ConnectorFactory<T, ?, ?> factory) {
        return (Class<T>) resolveType(factory.getClass()).getGeneric(0).resolve();
    }

    @SuppressWarnings("unchecked")
    public static <T extends RequestSpec> Class<T> getRequestSpecType(ConnectorFactory<?, T, ?> factory) {
        return (Class<T>) resolveType(factory.getClass()).getGeneric(1).resolve();
    }

    @SuppressWarnings("unchecked")
    public static <T extends ResponseSpec> Class<T> getResponseSpecType(ConnectorFactory<?, ?, T> factory) {
        return (Class<T>) resolveType(factory.getClass()).getGeneric(2).resolve();
    }
}