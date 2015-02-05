package com.github.ruediste.simpledi.standard.binder;

import java.lang.reflect.Constructor;

import javax.inject.Provider;

import com.github.ruediste.simpledi.core.ContextualInjector;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.InjectorConfiguration.MemberInjectionToken;
import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;
import com.github.ruediste.simpledi.standard.StandardStaticBinding;
import com.github.ruediste.simpledi.standard.recipe.RecipeCreationStep;
import com.github.ruediste.simpledi.standard.recipe.RecipeInstantiator;
import com.github.ruediste.simpledi.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class LinkedBindingBuilder<T> extends ScopedBindingBuilder<T> {

	public LinkedBindingBuilder(StandardStaticBinding binding,
			Dependency<T> eagerInstantiationKey,
			StandardInjectorConfiguration config) {
		super(binding, eagerInstantiationKey, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> to(Class<? extends T> implementation) {
		return to(TypeToken.of(implementation));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> to(TypeToken<? extends T> implementation) {

		binding.recipeCreationSteps.addFirst(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = config.createRecipeInstantiator(implementation);
			}
		});

		return new ScopedBindingBuilder<>(binding,
				eagerInstantiationDependency, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.github.ruediste.simpledi.core.inject.Injector#injectMembers
	 */
	public void toInstance(T instance) {
		MemberInjectionToken<T> token = config.config
				.getMemberInjectionToken(instance);
		binding.recipeCreationSteps.addFirst(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = injector -> token.getValue(injector);
			}
		});

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 */
	public ScopedBindingBuilder<T> toProvider(Provider<? extends T> provider) {
		MemberInjectionToken<Provider<? extends T>> token = config.config
				.getMemberInjectionToken(provider);
		binding.recipeCreationSteps.addFirst(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = x -> token.getValue(x).get();
			}
		});

		return new ScopedBindingBuilder<>(binding,
				eagerInstantiationDependency, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			Class<? extends javax.inject.Provider<? extends T>> providerType) {
		return toProvider(Dependency.of(providerType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			TypeToken<? extends javax.inject.Provider<? extends T>> providerType) {
		return toProvider(Dependency.of(providerType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			Dependency<? extends javax.inject.Provider<? extends T>> providerKey) {

		binding.recipeCreationSteps.addFirst(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = new RecipeInstantiator<Object>() {
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

		return new ScopedBindingBuilder<>(binding,
				eagerInstantiationDependency, config);

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	public <S extends T> ScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor) {
		return toConstructor(constructor, null);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	public <S extends T> ScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor, TypeToken<? extends S> type) {

		binding.recipeCreationSteps.addFirst(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = x -> config.createInstantiator(
						constructor, type);
			}
		});

		return new ScopedBindingBuilder<>(binding,
				eagerInstantiationDependency, config);

	}
}
