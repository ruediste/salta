package com.github.ruediste.salta.standard.binder;

import java.lang.reflect.Constructor;
import java.util.function.Function;

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
	public ScopedBindingBuilder<T> to(
			CoreDependencyKey<? extends T> implementation);

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.github.ruediste.salta.standard.core.inject.Injector#injectMembers
	 */
	public void toInstance(T instance);

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 */
	public ScopedBindingBuilder<T> toProvider(
			InstanceProvider<? extends T> provider);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			Class<? extends InstanceProvider<? extends T>> providerType);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			TypeToken<? extends InstanceProvider<? extends T>> providerType);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			CoreDependencyKey<? extends InstanceProvider<? extends T>> providerKey);

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * <p>
	 * This variant allows any provider class to be used as instance provider.
	 * However, a wrapper to {@link InstanceProvider} has to be provided
	 * </p>
	 */
	public <P> ScopedBindingBuilder<T> toProvider(
			CoreDependencyKey<P> providerKey,
			Function<? super P, InstanceProvider<? extends T>> providerWrapper);

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	public <S extends T> ScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor);

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	public <S extends T> ScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor, TypeToken<? extends S> type);
}
