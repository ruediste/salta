package com.github.ruediste.salta.jsr330.binder;

import java.lang.reflect.Constructor;

import javax.inject.Provider;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.standard.DependencyKey;
import com.google.common.reflect.TypeToken;

public class LinkedBindingBuilderImpl<T> extends ScopedBindingBuilderImpl<T>implements LinkedBindingBuilder<T> {

    private com.github.ruediste.salta.standard.binder.StandardLinkedBindingBuilder<T> delegate;

    public LinkedBindingBuilderImpl(
            com.github.ruediste.salta.standard.binder.StandardLinkedBindingBuilder<T> delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public ScopedBindingBuilder<T> to(Class<? extends T> implementation) {
        return new ScopedBindingBuilderImpl<>(delegate.to(implementation));
    }

    @Override
    public ScopedBindingBuilder<T> to(TypeToken<? extends T> implementation) {
        return new ScopedBindingBuilderImpl<>(delegate.to(implementation));
    }

    @Override
    public ScopedBindingBuilder<T> to(CoreDependencyKey<? extends T> implementation) {
        return new ScopedBindingBuilderImpl<>(delegate.to(implementation));
    }

    @Override
    public void toInstance(T instance) {
        delegate.toInstance(instance);

    }

    @Override
    public ScopedBindingBuilder<T> toProvider(Provider<? extends T> provider) {
        return new ScopedBindingBuilderImpl<>(delegate.toProviderInstance(provider, x -> () -> x.get()));
    }

    @Override
    public ScopedBindingBuilder<T> toProvider(Class<? extends Provider<? extends T>> providerType) {
        return toProvider(DependencyKey.of(providerType));
    }

    @Override
    public ScopedBindingBuilder<T> toProvider(TypeToken<? extends Provider<? extends T>> providerType) {
        return toProvider(DependencyKey.of(providerType));
    }

    @Override
    public <P extends Provider<? extends T>> ScopedBindingBuilder<T> toProvider(CoreDependencyKey<P> providerKey) {
        return new ScopedBindingBuilderImpl<>(delegate.toProvider(providerKey, p -> p.get()));
    }

    @Override
    public <S extends T> ScopedBindingBuilder<T> toConstructor(Constructor<S> constructor) {
        return toConstructor(constructor, TypeToken.of(constructor.getDeclaringClass()));
    }

    @Override
    public <S extends T> ScopedBindingBuilder<T> toConstructor(Constructor<S> constructor,
            TypeToken<? extends S> type) {
        return new ScopedBindingBuilderImpl<>(delegate.toConstructor(constructor, type));
    }

}
