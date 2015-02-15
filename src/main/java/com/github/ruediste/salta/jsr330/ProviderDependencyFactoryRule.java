package com.github.ruediste.salta.jsr330;

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
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.InjectionPoint;
import com.github.ruediste.salta.standard.Injector;
import com.google.common.reflect.TypeToken;

public class ProviderDependencyFactoryRule implements DependencyFactoryRule {

	static class ProviderAccessBeforeRecipeCreationFinishedException extends
			ProvisionException {
		private static final long serialVersionUID = 1L;

		ProviderAccessBeforeRecipeCreationFinishedException() {
			super(
					"Attempt to access injected Provider before the recipe creation finished. Known cause: accessing provider from constructor in singleton");
		}
	}

	static class ProviderAccessBeforeInstanceCreationFinishedException extends
			ProvisionException {
		private static final long serialVersionUID = 1L;

		ProviderAccessBeforeInstanceCreationFinishedException() {
			super(
					"Attempt to access injected Provider before the instance construction finished (e.g. from construction, injected method or post construct method)");
		}
	}

	private Injector injector;

	ProviderDependencyFactoryRule(Injector injector) {
		this.injector = injector;
	}

	private static class ProviderImpl implements Provider<Object> {
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

		ProviderImpl provider;

		public CreationRecipeImpl(CoreDependencyKey<?> dependency) {
			provider = new ProviderImpl(dependency);
		}

		@Override
		public void compile(GeneratorAdapter mv,
				RecipeCompilationContext compilationContext) {

			compilationContext.addAndLoad(Type.getDescriptor(Provider.class),
					provider);
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public CreationRecipe apply(CoreDependencyKey<?> dependency,
			RecipeCreationContext ctx) {

		if (Provider.class.equals(dependency.getRawType())) {
			// determine dependency
			TypeToken<?> providedType = dependency.getType().resolveType(
					Provider.class.getTypeParameters()[0]);

			CoreDependencyKey<?> dep;
			if (dependency instanceof InjectionPoint) {
				InjectionPoint p = (InjectionPoint) dependency;
				dep = new InjectionPoint(providedType, p.getMember(),
						p.getAnnotatedElement(), p.getParameterIndex());

			} else {
				dep = DependencyKey.of(providedType).withAnnotations(
						dependency.getAnnotatedElement().getAnnotations());
			}

			// create creation recipe
			CreationRecipeImpl creationRecipe = new CreationRecipeImpl(
					dependency);

			// queue creation and compilation of inner recipe
			ctx.queueAction(x -> {
				CreationRecipe innerRecipe = x.getRecipeInNewContext(dep);
				creationRecipe.provider.compiledRecipe = x
						.compileRecipe(innerRecipe);
			});

			return creationRecipe;

		}

		return null;
	}
}
