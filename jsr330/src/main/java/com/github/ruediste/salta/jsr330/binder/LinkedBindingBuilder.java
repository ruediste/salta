package com.github.ruediste.salta.jsr330.binder;

import java.lang.reflect.Constructor;

import javax.inject.Provider;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.google.common.reflect.TypeToken;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface LinkedBindingBuilder<T> extends ScopedBindingBuilder<T> {

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public ScopedBindingBuilder<T> to(Class<? extends T> implementation);

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public ScopedBindingBuilder<T> to(TypeToken<? extends T> implementation);

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public ScopedBindingBuilder<T> to(CoreDependencyKey<? extends T> implementation);

    /**
     * Bind to the specified instance. The members of the instance will be
     * injected. See the EDSL examples at {@link Binder}.
     */
    public void toInstance(T instance);

    /**
     * See the EDSL examples at {@link Binder}.
     *
     */
    public ScopedBindingBuilder<T> toProvider(Provider<? extends T> provider);

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public ScopedBindingBuilder<T> toProvider(Class<? extends Provider<? extends T>> providerType);

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public ScopedBindingBuilder<T> toProvider(TypeToken<? extends Provider<? extends T>> providerType);

    /**
     * See the EDSL examples at {@link Binder}.
     */
    public <P extends Provider<? extends T>> ScopedBindingBuilder<T> toProvider(CoreDependencyKey<P> providerKey);

    /**
     * See the EDSL examples at {@link Binder}.
     * 
     * @since 3.0
     */
    public <S extends T> ScopedBindingBuilder<T> toConstructor(Constructor<S> constructor);

    /**
     * See the EDSL examples at {@link Binder}.
     * 
     * @since 3.0
     */
    public <S extends T> ScopedBindingBuilder<T> toConstructor(Constructor<S> constructor, TypeToken<? extends S> type);
}
