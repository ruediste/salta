package com.github.ruediste.simpledi.binder;

import java.lang.reflect.Constructor;

import javax.inject.Provider;

import com.github.ruediste.simpledi.ContextualInjector;
import com.github.ruediste.simpledi.CreationRecipe;
import com.github.ruediste.simpledi.InjectorConfiguration;
import com.github.ruediste.simpledi.InstantiationResult;
import com.github.ruediste.simpledi.Instantiator;
import com.github.ruediste.simpledi.Dependency;
import com.github.ruediste.simpledi.Rule;
import com.github.ruediste.simpledi.matchers.Matcher;
import com.google.common.reflect.TypeToken;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class LinkedBindingBuilder<T> extends ScopedBindingBuilder {

	public LinkedBindingBuilder(Matcher<Dependency<?>> keyMatcher,
			Dependency<?> eagerInstantiationKey, InjectorConfiguration config) {
		super(keyMatcher, eagerInstantiationKey, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder to(Class<? extends T> implementation) {
		return to(TypeToken.of(implementation));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder to(TypeToken<? extends T> implementation) {
		return to(Dependency.of(implementation));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder to(Dependency<? extends T> targetKey) {
		config.addRule(new Rule() {

			@Override
			public void apply(CreationRecipe recipe, Dependency<?> key) {
				if (keyMatcher.matches(key))
					recipe.instantiator = new Instantiator<T>() {

						@Override
						public InstantiationResult<T> instantiate(Dependency<T> key,
								ContextualInjector injector) {
							return InstantiationResult.of(
									injector.createInstance(targetKey), true);
						}
					};
			}
		});

		return new ScopedBindingBuilder(keyMatcher, eagerInstantiationKey,
				config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.google.inject.Injector#injectMembers
	 */
	public void toInstance(T instance) {
		config.requestedMemberInjections.put(instance, instance);
		config.addRule(new Rule() {

			@Override
			public void apply(CreationRecipe recipe, Dependency<?> key) {
				if (keyMatcher.matches(key))
					recipe.instantiator = new Instantiator<T>() {

						@Override
						public InstantiationResult<T> instantiate(Dependency<T> key,
								ContextualInjector injector) {
							return InstantiationResult.of(instance, true);
						}
					};
			}
		});
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.google.inject.Injector#injectMembers
	 */
	public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
		config.requestedMemberInjections.put(provider, provider);
		config.addRule(new Rule() {

			@Override
			public void apply(CreationRecipe recipe, Dependency<?> key) {
				if (keyMatcher.matches(key))
					recipe.instantiator = new Instantiator<T>() {

						@Override
						public InstantiationResult<T> instantiate(Dependency<T> key,
								ContextualInjector injector) {
							return InstantiationResult.of(provider.get(), true);
						}
					};
			}
		});

		return new ScopedBindingBuilder(keyMatcher, eagerInstantiationKey,
				config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder toProvider(
			Class<? extends javax.inject.Provider<? extends T>> providerType) {
		return toProvider(Dependency.of(providerType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder toProvider(
			TypeToken<? extends javax.inject.Provider<? extends T>> providerType) {
		return toProvider(Dependency.of(providerType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder toProvider(
			Dependency<? extends javax.inject.Provider<? extends T>> providerKey) {
		config.addRule(new Rule() {

			@Override
			public void apply(CreationRecipe recipe, Dependency<?> key) {
				if (keyMatcher.matches(key))
					recipe.instantiator = new Instantiator<T>() {

						Provider<? extends T> provider;

						@Override
						public synchronized InstantiationResult<T> instantiate(
								Dependency<T> key, ContextualInjector injector) {
							if (provider == null)
								provider = injector.createInstance(providerKey);
							return InstantiationResult.of(provider.get(), true);
						}
					};
			}
		});

		return new ScopedBindingBuilder(keyMatcher, eagerInstantiationKey,
				config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	public <S extends T> ScopedBindingBuilder toConstructor(
			Constructor<S> constructor) {
		return toConstructor(constructor, null);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	public <S extends T> ScopedBindingBuilder toConstructor(
			Constructor<S> constructor, TypeToken<? extends S> type) {
		config.addRule(new Rule() {

			@Override
			public void apply(CreationRecipe recipe, Dependency<?> key) {
				if (keyMatcher.matches(key)) {
					recipe.constructor = constructor;
					if (type != null)
						recipe.constructorTypeToken = type;
				}
			}
		});

		return new ScopedBindingBuilder(keyMatcher, eagerInstantiationKey,
				config);

	}
}
