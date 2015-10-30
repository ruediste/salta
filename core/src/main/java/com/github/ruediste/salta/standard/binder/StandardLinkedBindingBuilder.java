package com.github.ruediste.salta.standard.binder;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.ruediste.salta.core.CoreDependencyKey;
import com.google.common.reflect.TypeToken;

/**
 * See the EDSL examples at {@link StandardBinder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface StandardLinkedBindingBuilder<T>
        extends StandardScopedBindingBuilder<T> {

    /**
     * See the EDSL examples at {@link StandardBinder}.
     */
    public StandardScopedBindingBuilder<T> to(
            Class<? extends T> implementation);

    /**
     * See the EDSL examples at {@link StandardBinder}.
     */
    public StandardScopedBindingBuilder<T> to(
            TypeToken<? extends T> implementation);

    /**
     * See the EDSL examples at {@link StandardBinder}.
     */
    public StandardScopedBindingBuilder<T> to(
            CoreDependencyKey<? extends T> implementation);

    /**
     * See the EDSL examples at {@link StandardBinder}.
     */
    public void toInstance(T instance);

    /**
     * See the EDSL examples at {@link StandardBinder}.
     *
     */
    public StandardScopedBindingBuilder<T> toProvider(
            Supplier<? extends T> provider);

    /**
     * See the EDSL examples at {@link StandardBinder}.
     * 
     * <p>
     * This variant allows any provider class to be used as instance provider.
     * However, a wrapper to {@link Supplier} has to be provided
     * </p>
     */
    public <P> StandardScopedBindingBuilder<T> toProvider(
            CoreDependencyKey<P> providerKey,
            Function<? super P, ? extends T> providerWrapper);

    /**
     * See the EDSL examples at {@link StandardBinder}.
     * 
     * @since 3.0
     */
    public <S extends T> StandardScopedBindingBuilder<T> toConstructor(
            Constructor<S> constructor);

    /**
     * See the EDSL examples at {@link StandardBinder}.
     * 
     * @since 3.0
     */
    public <S extends T> StandardScopedBindingBuilder<T> toConstructor(
            Constructor<S> constructor, TypeToken<? extends S> type);

    public <P> StandardScopedBindingBuilder<T> toProviderInstance(P provider,
            Function<P, Supplier<? extends T>> providerWrapper);
}
