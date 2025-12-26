package com.zwtech.flow.core.plugin;

import org.pf4j.PluginManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author renc
 */
public class SingletonSpringExtensionFactory extends SpringExtensionFactory {

    private static final ConcurrentMap<ClassLoader, ConcurrentMap<Class<?>, Object>> CACHE = new ConcurrentHashMap<>();

    public SingletonSpringExtensionFactory(PluginManager pluginManager) {
        super(pluginManager);
        pluginManager.addPluginStateListener(event -> {
            if (!event.getPluginState().isStarted()) {
                CACHE.remove(event.getPlugin().getPluginClassLoader());
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> extensionClass) {
        return (T) CACHE.computeIfAbsent(extensionClass.getClassLoader(), _ -> new ConcurrentHashMap<>())
                .computeIfAbsent(extensionClass, super::create);
    }

}