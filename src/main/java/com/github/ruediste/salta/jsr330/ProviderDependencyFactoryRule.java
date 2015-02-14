package com.github.ruediste.salta.jsr330;

import java.util.HashMap;

import javax.inject.Provider;

import org.mockito.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

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

	private HashMap<CoreDependencyKey<?>, ProviderImpl> currentProviders = new HashMap<>();

	private Injector injector;

	ProviderDependencyFactoryRule(Injector injector) {
		this.injector = injector;
	}

	private static class ProviderImpl implements Provider<Object> {
		Object value;
		boolean initialized;

		@Override
		public Object get() {
			if (!initialized) {
				throw new ProvisionException(
						"Attempt to access injected Provider before the construction finished (e.g. from construction, injected method or post construct method)");
			}
			return value;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public CreationRecipe apply(CoreDependencyKey<?> dependency,
			RecipeCreationContext ctx) {

		if (Provider.class.equals(dependency.getRawType())) {
			// break dependency circles
			ProviderImpl provider = currentProviders.get(dependency);
			if (provider == null) {

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

				// instantiate Provider
				provider = new ProviderImpl();
				currentProviders.put(dependency, provider);
				try {
					provider.value = injector.getInstance(dep);
					provider.initialized = true;
				} finally {
					currentProviders.remove(dependency);
				}
			}

			ProviderImpl tmp = provider;
			return new CreationRecipe() {

				@Override
				public void compile(GeneratorAdapter mv,
						RecipeCompilationContext compilationContext) {
					compilationContext.addAndLoad(
							Type.getDescriptor(Provider.class), tmp);
				}
			};

		}

		return null;
	}
}
