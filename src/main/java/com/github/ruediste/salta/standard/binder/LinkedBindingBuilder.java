package com.github.ruediste.salta.standard.binder;

import java.lang.reflect.Constructor;

import javax.inject.Provider;

import com.github.ruediste.salta.core.ContextualInjector;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.CreationRecipeFactory;
import com.github.ruediste.salta.standard.DefaultCreationRecipeFactory;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.MemberInjectionToken;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;
import com.google.common.reflect.TypeToken;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class LinkedBindingBuilder<T> extends ScopedBindingBuilder<T> {

	public LinkedBindingBuilder(Injector injector,
			StandardStaticBinding binding,
			DependencyKey<T> eagerInstantiationKey,
			StandardInjectorConfiguration config) {
		super(injector, binding, eagerInstantiationKey, config);
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
		binding.recipeFactory = new DefaultCreationRecipeFactory(config,
				implementation);

		return new ScopedBindingBuilder<>(injector, binding,
				eagerInstantiationDependency, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.github.ruediste.salta.standard.core.inject.Injector#injectMembers
	 */
	public void toInstance(T instance) {
		MemberInjectionToken<T> token = MemberInjectionToken
				.getMemberInjectionToken(injector, instance);
		binding.recipeFactory = () -> new CreationRecipe() {

			@Override
			public Object createInstance(ContextualInjector injector) {
				return token.getValue(injector);
			}

			@Override
			public void injectMembers(Object instance,
					ContextualInjector injector) {
				// NOP
			}
		};

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 */
	public ScopedBindingBuilder<T> toProvider(Provider<? extends T> provider) {
		MemberInjectionToken<Provider<? extends T>> token = MemberInjectionToken
				.getMemberInjectionToken(injector, provider);
		binding.recipeFactory = () -> new CreationRecipe() {

			@Override
			public Object createInstance(ContextualInjector injector) {
				return token.getValue(injector).get();
			}

			@Override
			public void injectMembers(Object instance,
					ContextualInjector injector) {
				// NOP
			}
		};

		return new ScopedBindingBuilder<>(injector, binding,
				eagerInstantiationDependency, config);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			Class<? extends javax.inject.Provider<? extends T>> providerType) {
		return toProvider(DependencyKey.of(providerType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			TypeToken<? extends javax.inject.Provider<? extends T>> providerType) {
		return toProvider(DependencyKey.of(providerType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			CoreDependencyKey<? extends javax.inject.Provider<? extends T>> providerKey) {

		binding.recipeFactory = () -> new CreationRecipe() {
			Provider<? extends T> provider;

			@Override
			public Object createInstance(ContextualInjector injector) {
				synchronized (this) {
					if (provider == null)
						provider = injector.getInstance(providerKey);
				}
				return provider.get();
			}

			@Override
			public void injectMembers(Object instance,
					ContextualInjector injector) {
				// NOP
			}
		};

		return new ScopedBindingBuilder<>(injector, binding,
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
		binding.recipeFactory = new CreationRecipeFactory() {

			@Override
			public CreationRecipe<?> createRecipe() {
				StandardCreationRecipe recipe = new DefaultCreationRecipeFactory(
						config, type).createRecipe();
				recipe.instantiator = config.fixedConstructorInstantiatorFactory
						.apply(constructor, type);
				return recipe;
			}
		};
		return new ScopedBindingBuilder<>(injector, binding,
				eagerInstantiationDependency, config);

	}
}
