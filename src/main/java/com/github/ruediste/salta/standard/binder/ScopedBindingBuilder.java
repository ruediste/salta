package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.DependencyKey;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.config.StandardInjectorConfiguration;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class ScopedBindingBuilder<T> {

	protected StandardStaticBinding binding;
	protected StandardInjectorConfiguration config;

	/**
	 * Dependency to be used to trigger an eager instantiation
	 */
	protected DependencyKey<T> eagerInstantiationDependency;
	protected Injector injector;

	public ScopedBindingBuilder(Injector injector,
			StandardStaticBinding binding,
			DependencyKey<T> eagerInstantiationDependency,
			StandardInjectorConfiguration config) {
		this.injector = injector;
		this.binding = binding;
		this.eagerInstantiationDependency = eagerInstantiationDependency;
		this.config = config;

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Class<? extends Annotation> scopeAnnotation) {
		binding.recipeFactory = () -> binding.recipeFactory.createRecipe()
				.withScope(config.getScope(scopeAnnotation));

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Scope scope) {
		binding.recipeFactory = () -> binding.recipeFactory.createRecipe()
				.withScope(scope);
	}

	/**
	 * Instructs the {@link Injector} to eagerly initialize this
	 * singleton-scoped binding upon creation. Useful for application
	 * initialization logic. See the EDSL examples at {@link Binder}.
	 */
	public void asEagerSingleton() {
		if (eagerInstantiationDependency == null) {
			throw new ProvisionException(
					"class to bind as eager singleton not known");
		}
		binding.recipeFactory = () -> binding.recipeFactory.createRecipe()
				.withScope(config.singletonScope);

		config.dynamicInitializers.add(x -> x
				.getInstance(eagerInstantiationDependency));
	}
}
