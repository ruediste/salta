package com.github.ruediste.salta.standard.binder;

import java.lang.reflect.Constructor;

import javax.inject.Provider;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.core.InjectorConfiguration.MemberInjectionToken;
import com.github.ruediste.salta.standard.FillDefaultsRecipeCreationStep;
import com.github.ruediste.salta.standard.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.recipe.RecipeCreationStep;
import com.github.ruediste.salta.standard.recipe.RecipeInstantiator;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;
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

		binding.recipeCreationSteps.clear();
		binding.recipeCreationSteps.add(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = config
						.createRecipeInstantiator(implementation);
			}
		});
		binding.recipeCreationSteps.add(new FillDefaultsRecipeCreationStep(
				config, implementation));

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
		binding.recipeCreationSteps.clear();
		binding.recipeCreationSteps.add(new RecipeCreationStep() {

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
		binding.recipeCreationSteps.clear();
		binding.recipeCreationSteps.add(new RecipeCreationStep() {

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

		binding.recipeCreationSteps.clear();
		binding.recipeCreationSteps.add(new RecipeCreationStep() {

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
		return toConstructor(constructor,
				TypeToken.of(constructor.getDeclaringClass()));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 * 
	 * @since 3.0
	 */
	public <S extends T> ScopedBindingBuilder<T> toConstructor(
			Constructor<S> constructor, TypeToken<? extends S> type) {
		binding.recipeCreationSteps.clear();
		binding.recipeCreationSteps.add(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
				if (recipe.instantiator != null)
					return;
				recipe.instantiator = x -> config.createInstantiator(
						constructor, type);
			}
		});
		binding.recipeCreationSteps.add(new FillDefaultsRecipeCreationStep(
				config, type));
		return new ScopedBindingBuilder<>(binding,
				eagerInstantiationDependency, config);

	}
}
