package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.core.Dependency;
import com.github.ruediste.salta.core.Injector;
import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.StandardInjectorConfiguration;
import com.github.ruediste.salta.standard.StandardStaticBinding;
import com.github.ruediste.salta.standard.recipe.RecipeCreationStep;
import com.github.ruediste.salta.standard.recipe.StandardCreationRecipe;

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
	protected Dependency<T> eagerInstantiationDependency;

	public ScopedBindingBuilder(StandardStaticBinding binding,
			Dependency<T> eagerInstantiationDependency,
			StandardInjectorConfiguration config) {
		this.binding = binding;
		this.eagerInstantiationDependency = eagerInstantiationDependency;
		this.config = config;

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Class<? extends Annotation> scopeAnnotation) {
		binding.recipeCreationSteps.addFirst(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
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
		binding.recipeCreationSteps.addFirst(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
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
		binding.recipeCreationSteps.addFirst(new RecipeCreationStep() {

			@Override
			public void accept(StandardCreationRecipe recipe) {
				if (recipe.scope != null)
					return;

				recipe.scope = config.singletonScope;
			}
		});

		config.config.dynamicInitializers.add(x -> x
				.createInstance(eagerInstantiationDependency));
	}
}
