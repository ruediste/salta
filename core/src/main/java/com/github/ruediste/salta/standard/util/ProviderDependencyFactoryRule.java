package com.github.ruediste.salta.standard.util;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.inject.Provider;

import org.mockito.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import com.github.ruediste.salta.core.CompiledCreationRecipe;
import com.github.ruediste.salta.core.CoreDependencyKey;
import com.github.ruediste.salta.core.CreationRecipe;
import com.github.ruediste.salta.core.DependencyFactoryRule;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.RecipeCompilationContext;
import com.github.ruediste.salta.core.RecipeCreationContext;
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

	public static class ProviderAccessBeforeRecipeCreationFinishedException
			extends ProvisionException {
		private static final long serialVersionUID = 1L;

		ProviderAccessBeforeRecipeCreationFinishedException() {
			super(
					"Attempt to access injected Provider before the recipe creation finished. Possible cause: accessing provider from constructor in singleton");
		}
	}

	public static class ProviderAccessBeforeInstanceCreationFinishedException
			extends ProvisionException {
		private static final long serialVersionUID = 1L;

		ProviderAccessBeforeInstanceCreationFinishedException() {
			super(
					"Attempt to access injected Provider before the instance construction finished (e.g. from construction, injected method or post construct method)");
		}
	}

	protected static class ProviderImpl implements Supplier<Object> {
		public CompiledCreationRecipe compiledRecipe;

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
			} catch (ProvisionException e) {
				throw e;
			} catch (Throwable e) {
				throw new ProvisionException(
						"Error while getting instance from provider for key "
								+ dependency, e);
			} finally {
				isGetting.remove();
			}
		}
	}

	private static class CreationRecipeImpl extends CreationRecipe {

		private Object wrappedProvider;

		public CreationRecipeImpl(Object wrappedProvider) {
			this.wrappedProvider = wrappedProvider;
		}

		@Override
		public void compile(GeneratorAdapter mv,
				RecipeCompilationContext compilationContext) {

			compilationContext.addAndLoad(
					Type.getDescriptor(wrappedProvider.getClass()),
					wrappedProvider);
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
			BiFunction<CoreDependencyKey<?>, Supplier<?>, Object> wrapper) {
		this.matcher = matcher;
		this.wrapper = wrapper;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public CreationRecipe apply(CoreDependencyKey<?> dependency,
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
					wrappedProvider);

			// queue creation and compilation of inner recipe
			ctx.queueAction(x -> {
				CreationRecipe innerRecipe = x.getRecipeInNewContext(dep);
				provider.compiledRecipe = x.compileRecipe(innerRecipe);
			});

			return creationRecipe;

		}

		return null;
	}
}
