package com.github.ruediste.salta.jsr330;

import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.StandardInjector;
import com.github.ruediste.salta.standard.config.MembersInjectionToken;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

public class InjectorImpl implements Injector {

    private StandardInjector delegate;

    public InjectorImpl(JSR330InjectorConfiguration config) {
        this.delegate = new StandardInjector(config.standardConfig);
    }

    public void initialize() {
        delegate.initialize();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void injectMembers(Object instance) {
        injectMembers((TypeToken) TypeToken.of(instance.getClass()), instance);
    }

    @Override
    public <T> void injectMembers(TypeToken<T> type, T instance) {
        getMembersInjector(type).injectMembers(instance);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return getMembersInjector(TypeToken.of(type));
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(TypeToken<T> typeLiteral) {
        TypeToken<MembersInjector<T>> injectorType = new TypeToken<MembersInjector<T>>() {
            private static final long serialVersionUID = 1L;
        }.where(new TypeParameter<T>() {
        }, typeLiteral);
        return getInstance(DependencyKey.of(injectorType));
    }

    @Override
    public <T> Provider<T> getProvider(CoreDependencyKey<T> key) {

        Supplier<T> result = delegate.getProvider(key);
        return new Provider<T>() {

            @Override
            public T get() {
                return result.get();
            }

            @Override
            public String toString() {
                return result.toString();
            }
        };
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        Supplier<T> result = delegate.getProvider(type);
        return new Provider<T>() {

            @Override
            public T get() {
                return result.get();
            }

            @Override
            public String toString() {
                return result.toString();
            }
        };
    }

    @Override
    public <T> T getInstance(CoreDependencyKey<T> key) {
        return delegate.getInstance(key);
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return delegate.getInstance(type);
    }

    @Override
    public <T> MembersInjectionToken<T> getMembersInjectionToken(T value) {
        return delegate.getMembersInjectionToken(value);
    }

    @Override
    public <T> MembersInjectionToken<T> getMembersInjectionToken(T value,
            TypeToken<T> type) {
        return delegate.getMembersInjectionToken(value, type);
    }

    @Override
    public StandardInjector getDelegate() {
        return delegate;
    }

    @Override
    public <T> Optional<T> tryGetInstance(CoreDependencyKey<T> key) {
        return delegate.tryGetInstance(key);
    }

    @Override
    public <T> Optional<T> tryGetInstance(Class<T> type) {
        return delegate.tryGetInstance(type);
    }

}
