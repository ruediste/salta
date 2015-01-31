package com.github.ruediste.simpledi.binding;

import java.lang.annotation.Annotation;

/**
 * See the EDSL examples at {@link Binder}.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface AnnotatedBindingBuilder<T> extends LinkedBindingBuilder<T> {

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	LinkedBindingBuilder<T> annotatedWith(
			Class<? extends Annotation> annotationType);

	/**
	 * See the EDSL examples at {@link Binder}.
	 */
	LinkedBindingBuilder<T> annotatedWith(Annotation annotation);
}
