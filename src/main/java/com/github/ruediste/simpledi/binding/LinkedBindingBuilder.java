package com.github.ruediste.simpledi.binding;

import java.lang.reflect.Constructor;

import javax.inject.Provider;

import com.github.ruediste.simpledi.Key;
import com.google.common.reflect.TypeToken;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface LinkedBindingBuilder<T> extends ScopedBindingBuilder {

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	ScopedBindingBuilder to(Class<? extends T> implementation);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	ScopedBindingBuilder to(TypeToken<? extends T> implementation);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	ScopedBindingBuilder to(Key<? extends T> targetKey);

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.google.inject.Injector#injectMembers
	 */
	void toInstance(T instance);

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.google.inject.Injector#injectMembers
	 */
	ScopedBindingBuilder toProvider(Provider<? extends T> provider);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	ScopedBindingBuilder toProvider(
			Class<? extends javax.inject.Provider<? extends T>> providerType);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	ScopedBindingBuilder toProvider(
			TypeToken<? extends javax.inject.Provider<? extends T>> providerType);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	ScopedBindingBuilder toProvider(
			Key<? extends javax.inject.Provider<? extends T>> providerKey);

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	<S extends T> ScopedBindingBuilder toConstructor(Constructor<S> constructor);

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	<S extends T> ScopedBindingBuilder toConstructor(
			Constructor<S> constructor, TypeToken<? extends S> type);
}
