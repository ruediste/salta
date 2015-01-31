package com.github.ruediste.simpledi.binding;

import java.lang.annotation.Annotation;

import com.github.ruediste.simpledi.Injector;
import com.github.ruediste.simpledi.Scope;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface ScopedBindingBuilder {

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
