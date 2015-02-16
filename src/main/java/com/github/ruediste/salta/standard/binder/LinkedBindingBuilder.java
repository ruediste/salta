package com.github.ruediste.salta.standard.binder;

import java.lang.reflect.Constructor;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import com.github.ruediste.salta.core.CompiledCreationRecipe;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
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
			extends ProvisionException {
		private static final long serialVersionUID = 1L;

		public RecursiveAccessOfInstanceOfProviderClassException(
				CoreDependencyKey<?> providerKey) {
			super(
					"Access of provider before creation finished. Circular dependency of provider class"
							+ providerKey);
		}
	}

	public static final class ProviderRecipeFactory implements
			CreationRecipeFactory {
		public final class ProviderImpl implements InstanceProvider<Object> {
			InstanceProvider<?> delegate;

			@Override
			public Object get() {
				if (delegate == null) {
					throw new RecursiveAccessOfInstanceOfProviderClassException(
							providerKey);
				}
				return delegate.get();
			}
		}

		private final class ProviderCreationRecipeImpl extends CreationRecipe {

			ProviderImpl provider = new ProviderImpl();

			@Override
			public void compile(GeneratorAdapter mv,
					RecipeCompilationContext compilationContext) {
				compilationContext.addAndLoad(
						Type.getDescriptor(ProviderImpl.class), provider);
				provider.get();
				mv.invokeVirtual(Type.getType(ProviderImpl.class),
						Method.getMethod("Object get()"));
			}
		}

		private CoreDependencyKey<?> providerKey;

		public ProviderRecipeFactory(CoreDependencyKey<?> providerKey) {
			this.providerKey = providerKey;
		}

		@Override
		public CreationRecipe createRecipe(RecipeCreationContext ctx) {

			ProviderCreationRecipeImpl recipe = new ProviderCreationRecipeImpl();

			ctx.queueAction(x -> {
				CreationRecipe innerRecipe = x
						.getRecipeInNewContext(providerKey);
				CompiledCreationRecipe compiledRecipe = x
						.compileRecipe(innerRecipe);
				recipe.provider.delegate = (InstanceProvider<?>) compiledRecipe
						.getNoThrow();
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
		data.binding.recipeFactory = new CreationRecipeFactory() {

			boolean recipeCreationInProgress;

			@Override
			public CreationRecipe createRecipe(RecipeCreationContext ctx) {
				if (recipeCreationInProgress) {
					throw new ProvisionException("Recipe creation in progress");
				}
				recipeCreationInProgress = true;
				T injected;
				try {
					injected = token.getValue();
				} finally {
					recipeCreationInProgress = false;
				}
				return new CreationRecipe() {

					@Override
					public void compile(GeneratorAdapter mv,
							RecipeCompilationContext compilationContext) {
						compilationContext.addAndLoad(
								Type.getDescriptor(injected.getClass()),
								injected);
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
			public CreationRecipe createRecipe(RecipeCreationContext ctx) {
				InstanceProvider<? extends T> injected = token.getValue();
				return new CreationRecipe() {

					@Override
					public void compile(GeneratorAdapter mv,
							RecipeCompilationContext compilationContext) {
						compilationContext.addAndLoad(
								Type.getDescriptor(InstanceProvider.class),
								injected);
						mv.invokeInterface(
								Type.getType(InstanceProvider.class),
								Method.getMethod("Object get()"));
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
			public CreationRecipe createRecipe(RecipeCreationContext ctx) {
				return builder.build(ctx);
			}
		};

		return new ScopedBindingBuilder<>(data);

	}
}
