package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.core.Scope;
import com.github.ruediste.salta.standard.Injector;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface ScopedBindingBuilder<T> {

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	void in(Class<? extends Annotation> scopeAnnotation);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	void in(Scope scope);

	/**
	 * Instructs the {@link Injector} to eagerly initialize this
	 * singleton-scoped binding upon creation. Useful for application
	 * initialization logic. See the EDSL examples at {@link Binder}.
	 */
	void asEagerSingleton();
}
