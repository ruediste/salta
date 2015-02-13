package com.github.ruediste.salta.standard.binder;

import java.lang.reflect.Constructor;

import com.github.ruediste.salta.core.BindingContext;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.standard.CreationRecipeFactory;
import com.github.ruediste.salta.standard.DefaultCreationRecipeBuilder;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.config.MemberInjectionToken;
import com.google.common.reflect.TypeToken;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class LinkedBindingBuilder<T> extends ScopedBindingBuilder<T> {

	private final class ProviderRecipeFactory implements CreationRecipeFactory {
		private InstanceProvider<? extends T> provider;

		public ProviderRecipeFactory(
				CoreDependencyKey<? extends InstanceProvider<? extends T>> providerKey) {
			data.config.dynamicInitializers.add(injector -> {
				provider = injector.getInstance(providerKey);
			});
		}

		@Override
		public CreationRecipe createRecipe(BindingContext ctx) {
			return new CreationRecipe() {

				@Override
				public Object createInstance() {
					return provider.get();
				}
			};
		}
	}

	public LinkedBindingBuilder(BindingBuilderData<T> data) {
		super(data);
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
		data.binding.recipeFactory = ctx -> new DefaultCreationRecipeBuilder(
				data.config, implementation, data.binding).build(ctx);

		return new ScopedBindingBuilder<>(data);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.github.ruediste.salta.standard.core.inject.Injector#injectMembers
	 */
	public void toInstance(T instance) {
		MemberInjectionToken<T> token = MemberInjectionToken
				.getMemberInjectionToken(data.injector, instance);
		data.binding.recipeFactory = (ctx) -> new CreationRecipe() {

			@Override
			public Object createInstance() {
				return token.getValue();
			}

		};

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 */
	public ScopedBindingBuilder<T> toProvider(
			InstanceProvider<? extends T> provider) {
		MemberInjectionToken<InstanceProvider<? extends T>> token = MemberInjectionToken
				.getMemberInjectionToken(data.injector, provider);
		data.binding.recipeFactory = (ctx) -> new CreationRecipe() {

			@Override
			public Object createInstance() {
				return token.getValue().get();
			}

		};

		return new ScopedBindingBuilder<>(data);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			Class<? extends InstanceProvider<? extends T>> providerType) {
		return toProvider(DependencyKey.of(providerType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			TypeToken<? extends InstanceProvider<? extends T>> providerType) {
		return toProvider(DependencyKey.of(providerType));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> toProvider(
			CoreDependencyKey<? extends InstanceProvider<? extends T>> providerKey) {

		data.binding.recipeFactory = new ProviderRecipeFactory(providerKey);

		return new ScopedBindingBuilder<>(data);

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
		DefaultCreationRecipeBuilder builder = new DefaultCreationRecipeBuilder(
				data.config, type, data.binding);
		builder.instantiatorSupplier = ctx -> data.config.fixedConstructorInstantiatorFactory
				.apply(constructor, type);

		data.binding.recipeFactory = new CreationRecipeFactory() {

			@Override
			public CreationRecipe createRecipe(BindingContext ctx) {
				return builder.build(ctx);
			}
		};

		return new ScopedBindingBuilder<>(data);

	}
}
