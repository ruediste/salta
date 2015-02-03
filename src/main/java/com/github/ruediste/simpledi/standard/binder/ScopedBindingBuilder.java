package com.github.ruediste.simpledi.standard.binder;

import java.lang.annotation.Annotation;
import java.util.function.Consumer;

import com.github.ruediste.simpledi.core.CreationRecipe;
import com.github.ruediste.simpledi.core.Dependency;
import com.github.ruediste.simpledi.core.Injector;
import com.github.ruediste.simpledi.core.ProvisionException;
import com.github.ruediste.simpledi.core.Scope;
import com.github.ruediste.simpledi.standard.StandardInjectorConfiguration;
import com.github.ruediste.simpledi.standard.StandardStaticBinding;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class ScopedBindingBuilder {

	protected StandardStaticBinding binding;
	protected StandardInjectorConfiguration config;

	/**
	 * Dependency to be used to trigger an eager instantiation
	 */
	protected Dependency<?> eagerInstantiationDependency;

	public ScopedBindingBuilder(StandardStaticBinding binding,
			Dependency<?> eagerInstantiationDependency,
			StandardInjectorConfiguration config) {
		this.binding = binding;
		this.eagerInstantiationDependency = eagerInstantiationDependency;
		this.config = config;

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Class<? extends Annotation> scopeAnnotation) {
		binding.recipeCreationSteps.addFirst(new Consumer<CreationRecipe>() {

			@Override
			public void accept(CreationRecipe recipe) {
				if (recipe.scope != null)
					return;
				Scope scope = config.scopeAnnotationMap.get(scopeAnnotation);
				if (scope == null)
					throw new ProvisionException("Unknown scope annotation "
							+ scopeAnnotation);
				recipe.scope = scope;
			}
		});
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Scope scope) {
		binding.recipeCreationSteps.addFirst(new Consumer<CreationRecipe>() {

			@Override
			public void accept(CreationRecipe recipe) {
				if (recipe.scope != null)
					return;
				recipe.scope = scope;
			}
		});
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
		binding.recipeCreationSteps.addFirst(new Consumer<CreationRecipe>() {

			@Override
			public void accept(CreationRecipe recipe) {
				if (recipe.scope != null)
					return;

				recipe.scope = config.singletonScope;
			}
		});

		config.requestedEagerInstantiations.add(eagerInstantiationDependency);
	}
}
