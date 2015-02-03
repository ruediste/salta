package com.github.ruediste.simpledi.standard.binder;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;

import javax.inject.Provider;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.CreationRecipe;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.standard.Instantiator;
import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;
import com.github.ruediste.simpledi.standard.StandardStaticBinding;
import com.google.common.reflect.TypeToken;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class LinkedBindingBuilder<T> extends ScopedBindingBuilder {

	public LinkedBindingBuilder(StandardStaticBinding binding,
			Dependency<?> eagerInstantiationKey,
			StandardInjectorConfiguration config) {
		super(binding, eagerInstantiationKey, config);
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

		binding.recipeCreationSteps.addFirst(new Consumer<CreationRecipe>() {

			@Override
			public void accept(CreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = config.createInstantiator(implementation);
			}
		});

		return new ScopedBindingBuilder(binding, eagerInstantiationDependency,
				config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.github.ruediste.simpledi.core.inject.Injector#injectMembers
	 */
	public void toInstance(T instance) {
		config.requestedMemberInjections.put(instance, instance);
		binding.recipeCreationSteps.addFirst(new Consumer<CreationRecipe>() {

			@Override
			public void accept(CreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = x -> instance;
			}
		});

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 */
	public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
		config.requestedMemberInjections.put(provider, provider);
		binding.recipeCreationSteps.addFirst(new Consumer<CreationRecipe>() {

			@Override
			public void accept(CreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = x -> provider.get();
			}
		});

		return new ScopedBindingBuilder(binding, eagerInstantiationDependency,
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

		binding.recipeCreationSteps.addFirst(new Consumer<CreationRecipe>() {

			@Override
			public void accept(CreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = new Instantiator<Object>() {
					Provider<? extends T> provider;

					@Override
					public Object instantiate(ContextualInjector injector) {
						synchronized (this) {
							if (provider == null)
								provider = injector.createInstance(providerKey);
						}
						return provider.get();
					}
				};
			}
		});

		return new ScopedBindingBuilder(binding, eagerInstantiationDependency,
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

		binding.recipeCreationSteps.addFirst(new Consumer<CreationRecipe>() {

			@Override
			public void accept(CreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = x -> config.createInstantiator(
						constructor, type);
			}
		});

		return new ScopedBindingBuilder(binding, eagerInstantiationDependency,
				config);

	}
}
