package com.github.ruediste.salta.standard.util;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.inject.Provider;

import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CompiledSupplier;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.DependencyFactoryRule;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.SaltaException;
import com.github.ruediste.salta.core.SupplierRecipe;
import com.github.ruediste.salta.matchers.Matcher;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.google.common.reflect.TypeToken;

/**
 * Base class for {@link DependencyFactoryRule}s which match some sort of
 * Provider (as in javax.inject.Provider).
 * 
 * <p>
 * The initialization of providers is quite delicate. This class makes
 * implementing such factories simple. Just provide a matcher which matches the
 * injection points you want to inject a provider into and a wrapper, which
 * creates an instance of the correct class, delegating to a supplied
 * {@link Supplier}.
 * </p>
 */
public class ProviderDependencyFactoryRule implements DependencyFactoryRule {

	private Matcher<? super CoreDependencyKey<?>> matcher;
	private BiFunction<CoreDependencyKey<?>, Supplier<?>, Object> wrapper;
	private Class<?> providerType;

	public static class ProviderAccessBeforeRecipeCreationFinishedException
			extends SaltaException {
		private static final long serialVersionUID = 1L;

		ProviderAccessBeforeRecipeCreationFinishedException() {
			super(
					"Attempt to access injected Provider before the recipe creation finished. Possible cause: accessing provider from constructor in singleton");
		}
	}

	public static class ProviderAccessBeforeInstanceCreationFinishedException
			extends SaltaException {
		private static final long serialVersionUID = 1L;

		ProviderAccessBeforeInstanceCreationFinishedException() {
			super(
					"Attempt to access injected Provider before the instance construction finished (e.g. from construction, injected method or post construct method)");
		}
	}

	protected static class ProviderImpl implements Supplier<Object> {
		public CompiledSupplier compiledRecipe;

		private ThreadLocal<Boolean> isGetting = new ThreadLocal<>();

		private CoreDependencyKey<?> dependency;

		public ProviderImpl(CoreDependencyKey<?> dependency) {
			this.dependency = dependency;
		}

		@Override
		public Object get() {
			if (compiledRecipe == null) {
				throw new ProviderAccessBeforeRecipeCreationFinishedException();
			}

			if (isGetting.get() != null)
				throw new ProviderAccessBeforeInstanceCreationFinishedException();
			isGetting.set(true);

			try {
				return compiledRecipe.get();
			} catch (SaltaException e) {
				throw e;
			} catch (Throwable e) {
				throw new SaltaException(
						"Error while getting instance from provider for key "
								+ dependency, e);
			} finally {
				isGetting.remove();
			}
		}
	}

	private static class CreationRecipeImpl<T> extends SupplierRecipe {

		private T wrappedProvider;
		private Class<T> providerType;

		public CreationRecipeImpl(Class<?> providedType, T wrappedProvider,
				Class<T> providerType) {
			this.wrappedProvider = wrappedProvider;
			this.providerType = providerType;
		}

		@Override
		public Class<?> compileImpl(GeneratorAdapter mv,
				RecipeCompilationContext compilationContext) {

			compilationContext.addFieldAndLoad(providerType, wrappedProvider);
			return providerType;
		}
	}

	/**
	 * Create a new instance
	 * 
	 * @param matcher
	 *            matcher for the injection points which should be injected with
	 *            the provider
	 * @param wrapper
	 *            wrapper of a supplier to the actual instance which gets
	 *            injected
	 */
	public ProviderDependencyFactoryRule(
			Matcher<? super CoreDependencyKey<?>> matcher,
			BiFunction<CoreDependencyKey<?>, Supplier<?>, Object> wrapper,
			Class<?> providerType) {
		this.matcher = matcher;
		this.wrapper = wrapper;
		this.providerType = providerType;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public SupplierRecipe apply(CoreDependencyKey<?> dependency,
			RecipeCreationContext ctx) {

		if (matcher.matches(dependency)) {
			// determine dependency
			TypeToken<?> requestedType = dependency.getType().resolveType(
					Provider.class.getTypeParameters()[0]);

			CoreDependencyKey<?> dep;
			if (dependency instanceof InjectionPoint) {
				InjectionPoint p = (InjectionPoint) dependency;
				dep = new InjectionPoint(requestedType, p.getMember(),
						p.getAnnotatedElement(), p.getParameterIndex());

			} else {
				dep = DependencyKey.of(requestedType).withAnnotations(
						dependency.getAnnotatedElement().getAnnotations());
			}

			// create and wrap provider instance
			ProviderImpl provider = new ProviderImpl(dependency);
			Object wrappedProvider = wrapper.apply(dependency, provider);

			// create creation recipe
			CreationRecipeImpl creationRecipe = new CreationRecipeImpl(
					providerType, wrappedProvider, providerType);

			// queue creation and compilation of inner recipe
			ctx.queueAction(() -> {
				SupplierRecipe innerRecipe = ctx.getRecipeInNewContext(dep);
				provider.compiledRecipe = ctx.getCompiler().compileSupplier(
						innerRecipe);
			});

			return creationRecipe;

		}

		return null;
	}
}
