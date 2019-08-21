package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import com.github.ruediste.salta.core.Scope;

/**
 * Since many API types are not present in the core, the documentation has been
 * moved to the Binder classes of the JSR330 or the Guice API.
 */
public interface StandardScopedBindingBuilder<T> {

	void in(Class<? extends Annotation> scopeAnnotation);

	void in(Scope scope);

	/**
	 * Instructs the injector to eagerly initialize this singleton-scoped binding
	 * upon creation. Useful for application initialization logic.
	 */
	void asEagerSingleton();
}
