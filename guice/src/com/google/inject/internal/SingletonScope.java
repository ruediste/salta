package com.google.inject.internal;

import com.github.ruediste.salta.core.Binding;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Singleton;

/**
 * One instance per {@link Injector}. Also see {@code @}{@link Singleton}.
 */
public class SingletonScope implements Scope {

    @Override
    public String toString() {
        return "Scopes.SINGLETON";
    }

    @Override
    public <T> Provider<T> scope(Binding binding,
            CoreDependencyKey<T> requestedKey, Provider<T> unscoped) {
        T instance = unscoped.get();
        return new Provider<T>() {

            @Override
            public T get() {
                return instance;
            }

            @Override
            public String toString() {
                return "SingletonProvider(" + requestedKey + ")";
            }
        };
    }
}
