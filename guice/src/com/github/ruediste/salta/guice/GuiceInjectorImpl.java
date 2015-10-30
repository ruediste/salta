package com.github.ruediste.salta.guice;

import java.util.function.Supplier;

import com.github.ruediste.salta.guice.binder.BindingImpl;
import com.github.ruediste.salta.guice.binder.GuiceInjectorConfiguration;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.StandardInjector;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

public class GuiceInjectorImpl implements Injector {

    private StandardInjector delegate;

    public GuiceInjectorImpl(GuiceInjectorConfiguration config) {
        delegate = new StandardInjector(config.config);
    }

    @Override
    public void injectMembers(Object instance) {
        delegate.injectMembers(instance);
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(
            TypeLiteral<T> typeLiteral) {
        TypeToken<MembersInjector<T>> injectorType = new TypeToken<MembersInjector<T>>() {
            private static final long serialVersionUID = 1L;
        }.where(new TypeParameter<T>() {
        }, typeLiteral.getTypeToken());
        return delegate.getInstance(DependencyKey.of(injectorType));
    }

    @Override
    public <T> MembersInjector<T> getMembersInjector(Class<T> type) {
        return getMembersInjector(TypeLiteral.get(type));
    }

    @Override
    public <T> Provider<T> getProvider(Key<T> key) {
        Supplier<T> provider = delegate.getProvider(new KeyAdapter<>(key));
        return new Provider<T>() {
            @Override
            public T get() {
                return provider.get();
            }

            @Override
            public String toString() {
                return provider.toString();
            }
        };
    }

    @Override
    public <T> Provider<T> getProvider(Class<T> type) {
        Supplier<T> provider = delegate.getProvider(type);
        return new Provider<T>() {
            @Override
            public T get() {
                return provider.get();
            }

            @Override
            public String toString() {
                return provider.toString();
            }
        };
    }

    @Override
    public <T> T getInstance(Key<T> key) {
        return delegate.getInstance(new KeyAdapter<>(key));
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return delegate.getInstance(type);
    }

    @Override
    public <T> Binding<T> getBinding(Key<T> key) {
        return new BindingImpl<>(key, getProvider(key));
    }

    @Override
    public <T> Binding<T> getBinding(Class<T> type) {

        return new BindingImpl<>(Key.get(type), getProvider(type));
    }

    @Override
    public StandardInjector getSaltaInjector() {
        return delegate;
    }

    public void initialize() {
        delegate.initialize();
    }

}
