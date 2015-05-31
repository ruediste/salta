package com.github.ruediste.salta.guice.binder;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

import javax.inject.Inject;

import com.github.ruediste.salta.guice.KeyAdapter;
import com.github.ruediste.salta.standard.DependencyKey;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

public class LinkedBindingBuilderImpl<T> extends ScopedBindingBuilderImpl
        implements LinkedBindingBuilder<T> {

    private com.github.ruediste.salta.standard.binder.StandardLinkedBindingBuilder<T> delegate;

    public LinkedBindingBuilderImpl(
            com.github.ruediste.salta.standard.binder.StandardLinkedBindingBuilder<T> delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public ScopedBindingBuilder to(Class<? extends T> implementation) {
        return new ScopedBindingBuilderImpl(delegate.to(implementation));
    }

    @Override
    public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
        return new ScopedBindingBuilderImpl(delegate.to(implementation
                .getTypeToken()));
    }

    @Override
    public ScopedBindingBuilder to(Key<? extends T> targetKey) {
        return new ScopedBindingBuilderImpl(delegate.to(KeyAdapter
                .of(targetKey)));
    }

    @Override
    public void toInstance(T instance) {
        delegate.toInstance(instance);
    }

    @Override
    public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {

        return new ScopedBindingBuilderImpl(
                delegate.toProvider(new Supplier<T>() {
                    @Override
                    public T get() {
                        return provider.get();
                    }

                    @Inject
                    public void init(Injector injector) {
                        injector.injectMembers(provider);
                    }
                }));
    }

    @Override
    public ScopedBindingBuilder toProvider(
            javax.inject.Provider<? extends T> provider) {
        return new ScopedBindingBuilderImpl(delegate.toProvider(provider::get));
    }

    @Override
    public ScopedBindingBuilder toProvider(
            Class<? extends javax.inject.Provider<? extends T>> providerType) {
        return toProvider(TypeLiteral.get(providerType));
    }

    @Override
    public ScopedBindingBuilder toProvider(
            TypeLiteral<? extends javax.inject.Provider<? extends T>> providerType) {
        return toProviderImpl(providerType);
    }

    private <P extends javax.inject.Provider<? extends T>> ScopedBindingBuilder toProviderImpl(
            TypeLiteral<P> providerType) {
        return new ScopedBindingBuilderImpl(delegate.toProvider(
                DependencyKey.of(providerType.getTypeToken()), t -> t.get()));
    }

    @Override
    public <P extends javax.inject.Provider<? extends T>> ScopedBindingBuilder toProvider(
            Key<P> providerKey) {
        return new ScopedBindingBuilderImpl(delegate.toProvider(
                KeyAdapter.of(providerKey), t -> t.get()));
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(
            Constructor<S> constructor) {
        return new ScopedBindingBuilderImpl(delegate.toConstructor(constructor));
    }

    @Override
    public <S extends T> ScopedBindingBuilder toConstructor(
            Constructor<S> constructor, TypeLiteral<? extends S> type) {
        return new ScopedBindingBuilderImpl(delegate.toConstructor(constructor,
                type.getTypeToken()));
    }

}
