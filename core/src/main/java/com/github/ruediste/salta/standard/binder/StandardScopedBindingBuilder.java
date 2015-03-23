package com.github.ruediste.salta.standard.binder;

import java.lang.annotation.Annotation;

import javax.xml.bind.Binder;

import com.github.ruediste.salta.core.Scope;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface StandardScopedBindingBuilder<T> {

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	void in(Class<? extends Annotation> scopeAnnotation);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	void in(Scope scope);

	/**
	 * Instructs the injector to eagerly initialize this singleton-scoped
	 * binding upon creation. Useful for application initialization logic. See
	 * the EDSL examples at {@link Binder}.
	 */
	void asEagerSingleton();
}
