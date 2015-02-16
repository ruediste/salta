package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.core.ProvisionException;
import com.github.ruediste.salta.standard.Injector;
import com.github.ruediste.salta.standard.Scope;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class ScopedBindingBuilder<T> {

	protected BindingBuilderData<T> data;

	public ScopedBindingBuilder(BindingBuilderData<T> data) {
		this.data = data;

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Class<? extends Annotation> scopeAnnotation) {
		data.recipeBuilder.scopeSupplier = () -> data.config
				.getScope(scopeAnnotation);

	}

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	public void in(Scope scope) {
		data.recipeBuilder.scopeSupplier = () -> scope;
	}

	/**
	 * Instructs the {@link Injector} to eagerly initialize this
	 * singleton-scoped binding upon creation. Useful for application
	 * initialization logic. See the EDSL examples at {@link Binder}.
	 */
	public void asEagerSingleton() {
		if (data.eagerInstantiationDependency == null) {
			throw new ProvisionException(
					"class to bind as eager singleton not known");
		}
		data.recipeBuilder.scopeSupplier = () -> data.config.singletonScope;

		data.config.dynamicInitializers.add(x -> x
				.getInstance(data.eagerInstantiationDependency));
	}
}
