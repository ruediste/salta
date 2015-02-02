package com.github.ruediste.simpledi.binding;

import java.lang.annotation.Annotation;

import com.github.ruediste.simpledi.CreationRecipe;
import com.github.ruediste.simpledi.Injector;
import com.github.ruediste.simpledi.InjectorConfiguration;
import com.github.ruediste.simpledi.InstanceRequest;
import com.github.ruediste.simpledi.ProvisionException;
import com.github.ruediste.simpledi.Rule;
import com.github.ruediste.simpledi.Scope;
import com.github.ruediste.simpledi.matchers.Matcher;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class ScopedBindingBuilder {

	protected Matcher<InstanceRequest<?>> keyMatcher;
	protected InjectorConfiguration config;
	protected InstanceRequest<?> eagerInstantiationKey;

	public ScopedBindingBuilder(Matcher<InstanceRequest<?>> keyMatcher,
			InstanceRequest<?> eagerInstantiationKey, InjectorConfiguration config) {
		this.keyMatcher = keyMatcher;
		this.eagerInstantiationKey = eagerInstantiationKey;
		this.config = config;

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Class<? extends Annotation> scopeAnnotation) {
		config.addRule(new Rule() {

			@Override
			public void apply(CreationRecipe recipe, InstanceRequest<?> key) {
				if (keyMatcher.matches(key)) {
					Scope scope = config.scopeAnnotationMap
							.get(scopeAnnotation);
					if (scope == null)
						throw new ProvisionException(
								"Unknown scope annotation " + scopeAnnotation);
					recipe.scope = scope;
				}
			}
		});
	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Scope scope) {
		config.addRule(new Rule() {

			@Override
			public void apply(CreationRecipe recipe, InstanceRequest<?> key) {
				if (keyMatcher.matches(key))
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
		if (eagerInstantiationKey == null) {
			throw new ProvisionException(
					"class to bind as eager singleton not known");
		}
		config.addRule(new Rule() {

			@Override
			public void apply(CreationRecipe recipe, InstanceRequest<?> key) {
				if (keyMatcher.matches(key))
					recipe.scope = config.singletonScope;
			}
		});
		config.requestedEagerInstantiations.add(eagerInstantiationKey);
	}
}
