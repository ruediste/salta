package com.github.ruediste.salta.standard.binder;

import java.lang.reflect.Constructor;
import java.util.function.Function;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.compile.MethodCompilationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipe;
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

	public static class RecursiveAccessOfInstanceOfProviderClassException
			extends SaltaException {
		private static final long serialVersionUID = 1L;

		public RecursiveAccessOfInstanceOfProviderClassException(
				CoreDependencyKey<?> providerKey) {
			super(
					"Access of provider before creation finished. Circular dependency of provider class"
							+ providerKey);
		}
	}

	public static final class ProviderRecipeFactory<T, P> implements
			CreationRecipeFactory {
		public final class ProviderImpl implements InstanceProvider<Object> {
			InstanceProvider<? extends T> delegate;

			@Override
			public Object get() {
				if (delegate == null) {
					throw new RecursiveAccessOfInstanceOfProviderClassException(
							providerKey);
				}
				return delegate.get();
			}
		}

		private final class ProviderCreationRecipeImpl extends SupplierRecipe {

			ProviderImpl provider = new ProviderImpl();

			@Override
			public Class<?> compileImpl(GeneratorAdapter mv,
					MethodCompilationContext compilationContext) {
				compilationContext
						.addFieldAndLoad(ProviderImpl.class, provider);
				mv.invokeVirtual(Type.getType(ProviderImpl.class),
						Method.getMethod("Object get()"));
				return Object.class;
			}
		}

		private CoreDependencyKey<P> providerKey;
		private Function<? super P, InstanceProvider<? extends T>> providerWrapper;
		private Class<?> producedType;

		public ProviderRecipeFactory(
				Class<?> producedType,
				CoreDependencyKey<P> providerKey,
				Function<? super P, InstanceProvider<? extends T>> providerWrapper) {
			this.producedType = producedType;
			this.providerKey = providerKey;
			this.providerWrapper = providerWrapper;
		}

		@SuppressWarnings("unchecked")
		@Override
		public SupplierRecipe createRecipe(RecipeCreationContext ctx) {

			ProviderCreationRecipeImpl recipe = new ProviderCreationRecipeImpl();

			ctx.queueAction(() -> {
				CompiledSupplier compiledRecipe = ctx
						.getCompiler()
						.compileSupplier(ctx.getRecipeInNewContext(providerKey));
				recipe.provider.delegate = providerWrapper
						.apply((P) compiledRecipe.getNoThrow());
			});
			return recipe;
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
		return to(DependencyKey.of(implementation));
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public ScopedBindingBuilder<T> to(
			CoreDependencyKey<? extends T> implementation) {
		data.binding.recipeFactory = ctx -> ctx.getRecipe(implementation);

		return new ScopedBindingBuilder<>(data);
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 *
	 * @see com.github.ruediste.salta.standard.core.inject.Injector#injectMembers
	 */
	public void toInstance(T instance) {
		if (instance == null)
			throw new SaltaException(
					"Binding to null instances is not allowed. Use toProvider(Providers.of(null))");

		MemberInjectionToken<T> token = MemberInjectionToken
				.getMemberInjectionToken(data.injector, instance);
		data.binding.recipeFactory = new CreationRecipeFactory() {

			boolean recipeCreationInProgress;

			@Override
			public SupplierRecipe createRecipe(RecipeCreationContext ctx) {
				if (recipeCreationInProgress) {
					throw new SaltaException("Recipe creation in progress");
				}
				recipeCreationInProgress = true;
				T injected;
				try {
					injected = token.getValue();
				} finally {
					recipeCreationInProgress = false;
				}
				return new SupplierRecipe() {

					@Override
					public Class<?> compileImpl(GeneratorAdapter mv,
							MethodCompilationContext compilationContext) {
						Class<? super T> cls = data.boundType.getRawType();
						compilationContext.addFieldAndLoad(cls, injected);
						return cls;
					}

				};
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
		data.binding.recipeFactory = new CreationRecipeFactory() {
			@Override
			public SupplierRecipe createRecipe(RecipeCreationContext ctx) {
				InstanceProvider<? extends T> injected = token.getValue();
				return new SupplierRecipe() {

					@Override
					public Class<Object> compileImpl(GeneratorAdapter mv,
							MethodCompilationContext compilationContext) {
						compilationContext.addFieldAndLoad(
								InstanceProvider.class, injected);
						mv.invokeInterface(
								Type.getType(InstanceProvider.class),
								Method.getMethod("Object get()"));
						return Object.class;
					}

				};
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

		return toProvider(providerKey, x -> x);

	}

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
			Function<? super P, InstanceProvider<? extends T>> providerWrapper) {
		data.binding.recipeFactory = new ProviderRecipeFactory<T, P>(
				data.boundType.getRawType(), providerKey, providerWrapper);

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
				.create(type, ctx, constructor);

		data.binding.recipeFactory = builder::build;

		return new ScopedBindingBuilder<>(data);

	}
}
